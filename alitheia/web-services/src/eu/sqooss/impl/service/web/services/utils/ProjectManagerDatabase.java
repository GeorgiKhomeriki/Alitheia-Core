/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.impl.service.web.services.utils;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import eu.sqooss.impl.service.web.services.datatypes.WSDeveloper;
import eu.sqooss.impl.service.web.services.datatypes.WSDirectory;
import eu.sqooss.impl.service.web.services.datatypes.WSFileGroup;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectFile;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectVersion;
import eu.sqooss.impl.service.web.services.datatypes.WSStoredProject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;

public class ProjectManagerDatabase implements ProjectManagerDBQueries {
    
    private DBService db;
    
    public ProjectManagerDatabase(DBService db) {
        this.db = db;
    }
    
    public WSStoredProject[] getEvaluatedProjects() {
        db.startDBSession();
        List<?> evaluatedProjects = db.doHQL(GET_EVALUATED_PROJECTS);
        WSStoredProject[] result = WSStoredProject.asArray(evaluatedProjects);
        db.commitDBSession();
        return result;
    }
    
    public WSStoredProject[] getStoredProjects(Map<String, Object> properties) {
        db.startDBSession();
        List<StoredProject> storedProjects = db.findObjectsByProperties(
                StoredProject.class, properties);
        WSStoredProject[] result = WSStoredProject.asArray(storedProjects);
        db.commitDBSession();
        return result;
    }
    
    public WSProjectVersion[] getProjectVersionsByProjectId(long projectId) {
        db.startDBSession();
        StoredProject storedProject = db.findObjectById(StoredProject.class, projectId);
        WSProjectVersion[] result = null;
        if (storedProject != null) {
            List<ProjectVersion> projectVersions = storedProject.getProjectVersions();
            result = WSProjectVersion.asArray(projectVersions);
        }
        db.commitDBSession();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public WSStoredProject[] getProjectsByIds(long[] ids) {
        Map<String, Collection> queryParameters = new Hashtable<String, Collection>(1);
        Collection<Long> idsCollection = new ArrayList<Long>(ids.length);
        for (long id : ids) {
            idsCollection.add(id);
        }
        queryParameters.put(GET_PROJECTS_BY_IDS_PARAM, idsCollection);
        db.startDBSession();
        List<?> projects = db.doHQL(GET_PROJECTS_BY_IDS, null, queryParameters);
        WSStoredProject[] result = WSStoredProject.asArray(projects);
        db.commitDBSession();
        return result;
    }

    @SuppressWarnings("unchecked")
    public WSProjectVersion[] getProjectVersionsByIds(long[] ids) {
        Map<String, Collection> queryParameters = new Hashtable<String, Collection>(1);
        Collection<Long> idsCollection = new ArrayList<Long>(ids.length);
        for (long id : ids) {
            idsCollection.add(id);
        }
        queryParameters.put(GET_PROJECT_VERSIONS_BY_IDS_PARAM, idsCollection);
        db.startDBSession();
        List<?> projectVersions = db.doHQL(GET_PROJECT_VERSIONS_BY_IDS, null, queryParameters);
        WSProjectVersion[] result = WSProjectVersion.asArray(projectVersions);
        db.commitDBSession();
        return result;
    }
    
    public WSProjectFile[] getFilesByProjectId(long projectId) {
        Map<String, Object> queryParameters = new Hashtable<String, Object>(1);
        queryParameters.put(GET_FILES_BY_PROJECT_ID_PARAM, projectId);

        db.startDBSession();
        List<?> projectFiles = db.doHQL(GET_FILES_BY_PROJECT_ID, queryParameters);
        WSProjectFile[] result = convertToWSProjectFiles(projectFiles);
        db.commitDBSession();
        return result;
    }
    
    public WSProjectFile[] getFilesByProjectVersionId(long projectVersionId) {
        Map<String, Object> queryParameters = new Hashtable<String, Object>(1);
        queryParameters.put(GET_FILES_BY_PROJECT_VERSION_ID_PARAM, projectVersionId);
        
        WSProjectFile[] result;
        try {
            db.startDBSession();
            List<?> projectVersionFiles = db.doSQL(GET_FILES_BY_PROJECT_VERSION_ID, queryParameters);
            result = convertToWSProjectFiles(projectVersionFiles);
        } catch (SQLException e) {
            result = null;
        } finally {
            if (db.isDBSessionActive()) {
                db.commitDBSession();
            }
        }
        return result;
    }
    
    public WSFileGroup[] getFileGroupsByProjectId(long projectVersionId) {
        Map<String, Object> queryParameters = new Hashtable<String, Object>(1);
        queryParameters.put(GET_FILE_GROUPS_BY_PROJECT_ID_PARAM, projectVersionId);
        
        db.startDBSession();
        List<?> projectFileGroups = db.doHQL(GET_FILE_GROUPS_BY_PROJECT_ID, queryParameters);
        WSFileGroup[] result = WSFileGroup.asArray(projectFileGroups);
        db.commitDBSession();
        return result;
    }
    
    public long getFilesNumberByProjectVersionId(long projectVersionId) {
        Map<String, Object> queryParameters = new Hashtable<String, Object>(1);
        queryParameters.put(GET_FILES_NUMBER_BY_PROJECT_VERSION_ID_PARAM, projectVersionId);
        
        long result = 0;
        try {
            db.startDBSession();
            List<?> projectVersionFilesNumber = db.doSQL(GET_FILES_NUMBER_BY_PROJECT_VERSION_ID, queryParameters);
            if (!projectVersionFilesNumber.isEmpty()) {
                result = ((BigInteger)projectVersionFilesNumber.get(0)).longValue();
            }
        } catch (SQLException e) {
            //do nothing
        } finally {
            if (db.isDBSessionActive()) {
                db.commitDBSession();
            }
        }
        return result;
    }
    
    public long getFilesNumberByProjectId(long projectId) {
        Map<String, Object> queryParameters = new Hashtable<String, Object>(1);
        queryParameters.put(GET_FILES_NUMBER_BY_PROJECT_ID_PARAM, projectId);
        
        long result = 0;
        db.startDBSession();
        List<?> projectFilesNumber = db.doHQL(GET_FILES_NUMBER_BY_PROJECT_ID, queryParameters);
        if (!projectFilesNumber.isEmpty()) {
            result = ((Long) projectFilesNumber.get(0)).longValue();
        }
        db.commitDBSession();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public WSDirectory[] getDirectoriesByIds(long[] ids) {
        Map<String, Collection> queryParameters = new Hashtable<String, Collection>();
        Collection<Long> idsCollection = new ArrayList<Long>();
        for (long id : ids) {
            idsCollection.add(id);
        }
        queryParameters.put(GET_DIRECTORIES_BY_IDS_PARAM, idsCollection);
        db.startDBSession();
        List<?> directories = db.doHQL(GET_DIRECTORIES_BY_IDS, null, queryParameters);
        WSDirectory[] result = WSDirectory.asArray(directories);
        db.commitDBSession();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public WSDeveloper[] getDevelopersByIds(long[] ids) {
        Map<String, Collection> queryParameters = new Hashtable<String, Collection>();
        Collection<Long> idsCollection = new ArrayList<Long>();
        for (long id : ids) {
            idsCollection.add(id);
        }
        queryParameters.put(GET_DEVELOPERS_BY_IDS_PARAM, idsCollection);
        db.startDBSession();
        List<?> developers = db.doHQL(GET_DEVELOPERS_BY_IDS, null, queryParameters);
        WSDeveloper[] result = WSDeveloper.asArray(developers);
        db.commitDBSession();
        return result;
    }
    
    private WSProjectFile[] convertToWSProjectFiles(List<?> projectFiles) {
        WSProjectFile[] result = null;
        if ((projectFiles != null) && (projectFiles.size() != 0)) {
            Object currentElem = projectFiles.get(0);
            if (currentElem instanceof ProjectFile) { //parse HQL
                result = WSProjectFile.asList(projectFiles);
            } else if (currentElem.getClass().isArray()) { //parse SQL
                result = new WSProjectFile[projectFiles.size()];
                BigInteger fileId;
                BigInteger projectVersionId;
                BigInteger directoryId;
                String fileName;
                String status;
                Boolean isDirectory;
                Object[] currentFile;
                WSProjectFile currentWSProjectFile;
                for (int i = 0; i < result.length; i++) {
                    currentFile = (Object[])projectFiles.get(i);
                    fileId = (BigInteger)currentFile[0];
                    directoryId = (BigInteger)currentFile[1];
                    fileName = (String)currentFile[2];
                    projectVersionId = (BigInteger)currentFile[3];
                    status = (String)currentFile[4];
                    isDirectory = (Boolean)currentFile[5];
                    
                    currentWSProjectFile = new WSProjectFile();
                    currentWSProjectFile.setId(fileId.longValue());
                    currentWSProjectFile.setDirectoryId(directoryId.longValue());
                    currentWSProjectFile.setDirectory(isDirectory);
                    currentWSProjectFile.setStatus(status);
                    currentWSProjectFile.setProjectVersionId(projectVersionId.longValue());
                    currentWSProjectFile.setFileName(fileName);
                    
                    result[i] = currentWSProjectFile;
                }
            }
        }
        return result;
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
