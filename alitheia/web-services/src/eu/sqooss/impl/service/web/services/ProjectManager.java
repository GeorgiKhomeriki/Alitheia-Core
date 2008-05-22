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

package eu.sqooss.impl.service.web.services;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import eu.sqooss.impl.service.web.services.datatypes.WSDeveloper;
import eu.sqooss.impl.service.web.services.datatypes.WSDirectory;
import eu.sqooss.impl.service.web.services.datatypes.WSFileGroup;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectFile;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectVersion;
import eu.sqooss.impl.service.web.services.datatypes.WSStoredProject;
import eu.sqooss.impl.service.web.services.utils.ProjectManagerDatabase;
import eu.sqooss.impl.service.web.services.utils.SecurityWrapper;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.security.SecurityManager;

public class ProjectManager extends AbstractManager {
    
    private Logger logger;
    private ProjectManagerDatabase dbWrapper;
    private SecurityWrapper securityWrapper;
    
    public ProjectManager(Logger logger, DBService db, SecurityManager security) {
        super(db);
        this.logger = logger;
        this.dbWrapper = new ProjectManagerDatabase(db);
        this.securityWrapper = new SecurityWrapper(security);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getEvaluatedProjects(String, String)
     */
    public WSStoredProject[] getEvaluatedProjects(String userName, String password) {
        logger.info("Gets the evaluated project list! user: " + userName);

        securityWrapper.checkDBReadAccess(userName, password);
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> projects = dbWrapper.getEvaluatedProjects();
        WSStoredProject[] wsSP = WSStoredProject.asArray(projects);
        db.commitDBSession();
        return (WSStoredProject[]) normalizeWSArrayResult(wsSP);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getStoredProjects(String, String)
     */
    public WSStoredProject[] getStoredProjects(String userName, String password) {
        logger.info("Gets the stored project list! user: " + userName);

        securityWrapper.checkDBReadAccess(userName, password);
        
        super.updateUserActivity(userName);
    
        db.startDBSession();
        List queryResult = dbWrapper.getStoredProjects();

        List<StoredProject> l = (List<StoredProject>) queryResult;
        if (l==null) {
            logger.warn("Stored project query is broken.");
            db.rollbackDBSession();
            return null;
        } else {
            WSStoredProject[] wsSP = WSStoredProject.asArray(l);
            db.commitDBSession();
            return (WSStoredProject[]) normalizeWSArrayResult(wsSP);
        }
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getProjectByName(String, String, String)
     */
    public WSStoredProject getProjectByName(String userName, String password, String projectName) {

        logger.info("Retrieve project! user: " + userName +
                "; project name: " + projectName);

        db.startDBSession();
        List<StoredProject> projects = dbWrapper.getStoredProjects(projectName);
        db.commitDBSession();
                
        if (projects.size() != 0) {
            StoredProject storedProject = projects.get(0); 
            securityWrapper.checkProjectsReadAccess(
                    userName, password, new long[] {storedProject.getId()});
            super.updateUserActivity(userName);
            return WSStoredProject.getInstance(storedProject);
        } else {
            return null;
        }
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getProjectVersionsByProjectId(String, String, long)
     */
    public WSProjectVersion[] getProjectVersionsByProjectId(String userName, String password, long projectId) {

        logger.info("Retrieve stored project versions! user: " + userName +
                "; project's id: " + projectId);

        securityWrapper.checkProjectsReadAccess(
                userName, password, new long[] {projectId});

        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> storedProjects = dbWrapper.getProjectsByIds(new long[] {projectId});

        if (!storedProjects.isEmpty()) {
            StoredProject storedProject = (StoredProject) storedProjects.get(0);
            List<ProjectVersion> projectVersions = storedProject.getProjectVersions();
            WSProjectVersion[] wspv = WSProjectVersion.asArray(projectVersions);
            db.commitDBSession();
            return (WSProjectVersion[]) normalizeWSArrayResult(wspv);
        } else {
            db.rollbackDBSession();
            return null;
        }

    }
    
    public WSProjectVersion[] getProjectVersionsByIds(String userName,
            String password, long[] projectVersionsIds) {

        logger.info("Retrieve project versions! user: " + userName +
                "; project versions' ids: " + Arrays.toString(projectVersionsIds));

        securityWrapper.checkProjectVersionsReadAccess(
                userName, password, projectVersionsIds);

        super.updateUserActivity(userName);

        if(projectVersionsIds == null) {
            return null;
        }
        
        db.startDBSession();
        List<?> wspv = dbWrapper.getProjectVersionsByIds(projectVersionsIds);
        db.commitDBSession();

        return (WSProjectVersion[]) normalizeWSArrayResult(WSProjectVersion.asArray(wspv));
    }
    
    /**
     *  @see eu.sqooss.service.web.services.WebServices#getProjectsByIds(String, String, long[])
     */
    public WSStoredProject[] getProjectsByIds(String userName, String password, long[] projectsIds) {

        logger.info("Retrieve stored projects! user: " + userName +
                "; projects' ids: " + Arrays.toString(projectsIds) );

        securityWrapper.checkProjectsReadAccess(userName, password, projectsIds);

        super.updateUserActivity(userName);
        
        if (projectsIds == null) {
            return null;
        }
        
        db.startDBSession();
        List<?> wssp = dbWrapper.getProjectsByIds(projectsIds);
        db.commitDBSession();
        
        return (WSStoredProject[]) normalizeWSArrayResult(WSStoredProject.asArray(wssp));
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getFilesByProjectId(String, String, String)
     */
    public WSProjectFile[] getFilesByProjectId(String userName, String password, long projectId) {
        logger.info("Retrieve file list! user: " + userName + "; project id: " + projectId);

        securityWrapper.checkProjectsReadAccess(
                userName, password, new long[] {projectId});

        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getFilesByProjectId(projectId);

        WSProjectFile[] wspf = convertToWSProjectFiles(queryResult);
        db.commitDBSession();
        return (WSProjectFile[]) normalizeWSArrayResult(wspf);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getFilesByProjectVersionId(String, String, long)
     */
    public WSProjectFile[] getFilesByProjectVersionId(String userName, String password, long projectVersionId) {
        logger.info("Get file list for project version! user: " + userName +
                "; project version id: " + projectVersionId);
        
        securityWrapper.checkProjectVersionsReadAccess(
                userName, password, new long[] {projectVersionId});
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getFilesByProjectVersionId(projectVersionId);
        
        WSProjectFile[] wspf = convertToWSProjectFiles(queryResult);
        db.commitDBSession();
        return (WSProjectFile[]) normalizeWSArrayResult(wspf);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getFileGroupsByProjectId(String, String, long)
     */
    public WSFileGroup[] getFileGroupsByProjectId(String userName,
            String password, long projectId) {
        logger.info("Get a file group list for the project! user: " + userName +
                "; project id: " + projectId);
        
        securityWrapper.checkProjectsReadAccess(
                userName, password, new long[] {projectId});
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getFileGroupsByProjectId(projectId);
        WSFileGroup[] wsfg = WSFileGroup.asArray(queryResult);
        db.commitDBSession();
        
        return (WSFileGroup[]) normalizeWSArrayResult(wsfg);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getFilesNumberByProjectVersionId(String, String, long)
     */
    public long getFilesNumberByProjectVersionId(String userName, String password, long projectVersionId) {
        logger.info("Get files's number for project version! user: " + userName +
                "; project version id: " + projectVersionId);
        
        securityWrapper.checkProjectVersionsReadAccess(
                userName, password, new long[] {projectVersionId});
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getFilesNumberByProjectVersionId(projectVersionId);
        db.commitDBSession();
        return ((BigInteger)queryResult.get(0)).longValue();
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getFilesNumberByProjectId(String, String, long)
     */
    public long getFilesNumberByProjectId(String userName, String password, long projectId) {
        logger.info("Get file's number for project! user: " + userName +
                "; project id: " + projectId);
        
        securityWrapper.checkProjectsReadAccess(
                userName, password, new long[] {projectId});
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getFilesNumberByProjectId(projectId);
        db.commitDBSession();
        return ((Long) queryResult.get(0)).longValue();
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getDirectoriesByIds(String, String, long[])
     */
    public WSDirectory[] getDirectoriesByIds(String userName, String password,
            long[] directoriesIds) {
        logger.info("Get directories by ids! user: " + userName +
                "; directories' ids: " + Arrays.toString(directoriesIds));
        
        securityWrapper.checkDBReadAccess(userName, password);
        
        super.updateUserActivity(userName);
        
        db.startDBSession();
        List<?> queryResult = dbWrapper.getDirectoriesByIds(directoriesIds);
        db.commitDBSession();
        
        return (WSDirectory[]) normalizeWSArrayResult(WSDirectory.asArray(queryResult));
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getDevelopersByIds(String, String, long[])
     */
    public WSDeveloper[] getDevelopersByIds(String userName, String password,
            long[] developersIds) {
        logger.info("Get developers by ids! useR: " + userName +
                "; developers' ids: " + Arrays.toString(developersIds));
        
        securityWrapper.checkDBReadAccess(userName, password);
        
        super.updateUserActivity(userName);
        
        if (developersIds == null) {
            return null;
        }
        
        db.startDBSession();
        List<?> developers = dbWrapper.getDevelopersByIds(developersIds);
        db.commitDBSession();
        
        return (WSDeveloper[]) normalizeWSArrayResult(WSDeveloper.asArray(developers));
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
