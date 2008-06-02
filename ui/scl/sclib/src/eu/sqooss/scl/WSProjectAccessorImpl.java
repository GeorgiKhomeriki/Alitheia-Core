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

package eu.sqooss.scl;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.axis2.AxisFault;

import eu.sqooss.scl.accessor.WSProjectAccessor;
import eu.sqooss.ws.client.WsStub;
import eu.sqooss.ws.client.datatypes.WSDeveloper;
import eu.sqooss.ws.client.datatypes.WSDirectory;
import eu.sqooss.ws.client.datatypes.WSFileGroup;
import eu.sqooss.ws.client.datatypes.WSProjectFile;
import eu.sqooss.ws.client.datatypes.WSProjectVersion;
import eu.sqooss.ws.client.datatypes.WSStoredProject;
import eu.sqooss.ws.client.ws.GetDevelopersByIds;
import eu.sqooss.ws.client.ws.GetDevelopersByIdsResponse;
import eu.sqooss.ws.client.ws.GetDirectoriesByIds;
import eu.sqooss.ws.client.ws.GetDirectoriesByIdsResponse;
import eu.sqooss.ws.client.ws.GetEvaluatedProjects;
import eu.sqooss.ws.client.ws.GetEvaluatedProjectsResponse;
import eu.sqooss.ws.client.ws.GetFileGroupsByProjectId;
import eu.sqooss.ws.client.ws.GetFileGroupsByProjectIdResponse;
import eu.sqooss.ws.client.ws.GetFilesByProjectId;
import eu.sqooss.ws.client.ws.GetFilesByProjectIdResponse;
import eu.sqooss.ws.client.ws.GetFilesByProjectVersionId;
import eu.sqooss.ws.client.ws.GetFilesByProjectVersionIdResponse;
import eu.sqooss.ws.client.ws.GetFilesNumberByProjectId;
import eu.sqooss.ws.client.ws.GetFilesNumberByProjectIdResponse;
import eu.sqooss.ws.client.ws.GetFilesNumberByProjectVersionId;
import eu.sqooss.ws.client.ws.GetFilesNumberByProjectVersionIdResponse;
import eu.sqooss.ws.client.ws.GetLastProjectVersions;
import eu.sqooss.ws.client.ws.GetLastProjectVersionsResponse;
import eu.sqooss.ws.client.ws.GetProjectVersionsByIds;
import eu.sqooss.ws.client.ws.GetProjectVersionsByIdsResponse;
import eu.sqooss.ws.client.ws.GetProjectsByIds;
import eu.sqooss.ws.client.ws.GetProjectsByIdsResponse;
import eu.sqooss.ws.client.ws.GetProjectByName;
import eu.sqooss.ws.client.ws.GetProjectByNameResponse;
import eu.sqooss.ws.client.ws.GetProjectVersionsByProjectId;
import eu.sqooss.ws.client.ws.GetProjectVersionsByProjectIdResponse;
import eu.sqooss.ws.client.ws.GetStoredProjects;
import eu.sqooss.ws.client.ws.GetStoredProjectsResponse;

class WSProjectAccessorImpl extends WSProjectAccessor {

    private static final String METHOD_NAME_GET_EVALUATED_PROJECTS       = "getEvaluatedProjects";

    private static final String METHOD_NAME_GET_STORED_PROJECTS          = "getStoredProjects";

    private static final String METHOD_NAME_GET_FILES_BY_PROJECT_ID      = "getFilesByProjectId";

    private static final String METHOD_NAME_GET_PROJECT_BY_NAME          = "getProjectByName";

    private static final String METHOD_NAME_GET_PROJECT_VERSIONS_BY_PROJECT_ID       = "getProjectVersionsByProjectId";
    
    private static final String METHOD_NAME_GET_PROJECT_VERSIONS_BY_IDS              = "getProjectVersionsByIds";
    
    private static final String METHOD_NAME_GET_LAST_PROJECT_VERSIONS                = "getLastProjectVersions";

    private static final String METHOD_NAME_GET_PROJECTS_BY_IDS                      = "getProjectsByIds";

    private static final String METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_VERSION_ID   = "getFilesNumberByProjectVersionId";
    
    private static final String METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_ID           = "getFilesNumberByProjectId";

    private static final String METHOD_NAME_GET_FILES_BY_PROJECT_VERSION_ID          = "getFilesByProjectVersionId";
    
    private static final String METHOD_NAME_GET_FILE_GROUPS_BY_PROJECT_ID            = "getFileGroupsByProjectId";
    
    private static final String METHOD_NAME_GET_DIRECTORIES_BY_IDS                   = "getDirectoriesByIds";
    
    private static final String METHOD_NAME_GET_DEVELOPERS_BY_IDS                    = "getDevelopersByIds";

    private static final WSStoredProject[] EMPTY_ARRAY_STORED_PROJECTS   = new WSStoredProject[0];
    private static final WSProjectVersion[] EMPTY_ARRAY_PROJECT_VERSIONS = new WSProjectVersion[0];
    private static final WSDirectory[] EMPTY_ARRAY_DIRECTORIES = new WSDirectory[0];
    private static final WSDeveloper[] EMPTY_ARRAY_DEVELOPERS = new WSDeveloper[0];
    
    private Map<String, Object> parameters;
    private String userName;
    private String password;
    private WsStub wsStub;

    public WSProjectAccessorImpl(String userName, String password, String webServiceUrl) throws WSException {
        this.userName = userName;
        this.password = password;
        parameters = new Hashtable<String, Object>();
        try {
            this.wsStub = new WsStub(webServiceUrl);
        } catch (AxisFault af) {
            throw new WSException(af);
        }
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getEvaluatedProjects()
     */
    @Override
    public WSStoredProject[] getEvaluatedProjects() throws WSException {
        GetEvaluatedProjectsResponse response;
        GetEvaluatedProjects params;
        if (!parameters.containsKey(METHOD_NAME_GET_EVALUATED_PROJECTS)) {
            params = new GetEvaluatedProjects();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_EVALUATED_PROJECTS, params);
        } else {
            params = (GetEvaluatedProjects) parameters.get(
                    METHOD_NAME_GET_EVALUATED_PROJECTS);
        }
        synchronized (params) {
            try {
                response = wsStub.getEvaluatedProjects(params);
            } catch (RemoteException e) {
                throw new WSException(e);
            }
        }
        return (WSStoredProject[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getStoredProjects()
     */
    @Override
    public WSStoredProject[] getStoredProjects() throws WSException {
        GetStoredProjectsResponse response;
        GetStoredProjects params;
        if (!parameters.containsKey(METHOD_NAME_GET_STORED_PROJECTS)) {
            params = new GetStoredProjects();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_STORED_PROJECTS, params);
        } else {
            params = (GetStoredProjects) parameters.get(
                    METHOD_NAME_GET_STORED_PROJECTS);
        }
        synchronized (params) {
            try {
                response = wsStub.getStoredProjects(params);
            } catch (RemoteException e) {
                throw new WSException(e);
            }
        }
        return (WSStoredProject[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFilesByProjectId(long)
     */
    @Override
    public WSProjectFile[] getFilesByProjectId(long projectId) throws WSException {
        GetFilesByProjectIdResponse response;
        GetFilesByProjectId params;
        if (!parameters.containsKey(METHOD_NAME_GET_FILES_BY_PROJECT_ID)) {
            params = new GetFilesByProjectId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_FILES_BY_PROJECT_ID, params);
        } else {
            params = (GetFilesByProjectId) parameters.get(
                    METHOD_NAME_GET_FILES_BY_PROJECT_ID);
        }
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.getFilesByProjectId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSProjectFile[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFilesByProjectVersionId(long)
     */
    @Override
    public WSProjectFile[] getFilesByProjectVersionId(long projectVersionId) throws WSException {
        GetFilesByProjectVersionIdResponse response;
        GetFilesByProjectVersionId params;
        if (!parameters.containsKey(METHOD_NAME_GET_FILES_BY_PROJECT_VERSION_ID)) {
            params = new GetFilesByProjectVersionId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_FILES_BY_PROJECT_VERSION_ID, params);
        } else {
            params = (GetFilesByProjectVersionId) parameters.get(
                    METHOD_NAME_GET_FILES_BY_PROJECT_VERSION_ID);
        }
        synchronized (params) {
            params.setProjectVersionId(projectVersionId);
            try {
                response = wsStub.getFilesByProjectVersionId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSProjectFile[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFileGroupsByProjectId(long)
     */
    @Override
    public WSFileGroup[] getFileGroupsByProjectId(long projectId) throws WSException {
        GetFileGroupsByProjectIdResponse response;
        GetFileGroupsByProjectId params;
        if (!parameters.containsKey(METHOD_NAME_GET_FILE_GROUPS_BY_PROJECT_ID)) {
            params = new GetFileGroupsByProjectId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_FILE_GROUPS_BY_PROJECT_ID, params);
        } else {
            params = (GetFileGroupsByProjectId) parameters.get(
                    METHOD_NAME_GET_FILE_GROUPS_BY_PROJECT_ID);
        }
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.getFileGroupsByProjectId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSFileGroup[]) normalizeWSArrayResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFilesNumberByProjectVersionId(long)
     */
    @Override
    public long getFilesNumberByProjectVersionId(long projectVersionId) throws WSException {
        GetFilesNumberByProjectVersionIdResponse response;
        GetFilesNumberByProjectVersionId params;
        if (!parameters.containsKey(METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_VERSION_ID)) {
            params = new GetFilesNumberByProjectVersionId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_VERSION_ID, params);
        } else {
            params = (GetFilesNumberByProjectVersionId) parameters.get(
                    METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_VERSION_ID);
        }
        synchronized (params) {
            params.setProjectVersionId(projectVersionId);
            try {
                response = wsStub.getFilesNumberByProjectVersionId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }

        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFilesNumberByProjectId(long)
     */
    @Override
    public long getFilesNumberByProjectId(long projectId) throws WSException {
        GetFilesNumberByProjectIdResponse response;
        GetFilesNumberByProjectId params;
        if (!parameters.containsKey(METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_ID)) {
            params = new GetFilesNumberByProjectId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_ID, params);
        } else {
            params = (GetFilesNumberByProjectId) parameters.get(
                    METHOD_NAME_GET_FILES_NUMBER_BY_PROJECT_ID);
        }
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.getFilesNumberByProjectId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getDirectoriesByIds(long[])
     */
    @Override
    public WSDirectory[] getDirectoriesByIds(long[] directoriesIds)
            throws WSException {
        if (!normalizeWSArrayParameter(directoriesIds)) return EMPTY_ARRAY_DIRECTORIES;
        GetDirectoriesByIdsResponse response;
        GetDirectoriesByIds params;
        if (!parameters.containsKey(METHOD_NAME_GET_DIRECTORIES_BY_IDS)) {
            params = new GetDirectoriesByIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_DIRECTORIES_BY_IDS, params);
        } else {
            params = (GetDirectoriesByIds) parameters.get(
                    METHOD_NAME_GET_DIRECTORIES_BY_IDS);
        }
        synchronized (params) {
            params.setDirectoriesIds(directoriesIds);
            try {
                response = wsStub.getDirectoriesByIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSDirectory[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getDevelopersByIds(long[])
     */
    @Override
    public WSDeveloper[] getDevelopersByIds(long[] developersIds)
            throws WSException {
        if (!normalizeWSArrayParameter(developersIds)) return EMPTY_ARRAY_DEVELOPERS;
        GetDevelopersByIdsResponse response;
        GetDevelopersByIds params;
        if (!parameters.containsKey(METHOD_NAME_GET_DEVELOPERS_BY_IDS)) {
            params = new GetDevelopersByIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_DEVELOPERS_BY_IDS, params);
        } else {
            params = (GetDevelopersByIds) parameters.get(
                    METHOD_NAME_GET_DEVELOPERS_BY_IDS);
        }
        synchronized (params) {
            params.setDevelopersIds(developersIds);
            try {
                response = wsStub.getDevelopersByIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSDeveloper[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getProjectByName(java.lang.String)
     */
    @Override
    public WSStoredProject getProjectByName(String projectName) throws WSException {
        GetProjectByNameResponse response;
        GetProjectByName params;
        if (!parameters.containsKey(METHOD_NAME_GET_PROJECT_BY_NAME)) {
            params = new GetProjectByName();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_PROJECT_BY_NAME, params);
        } else {
            params = (GetProjectByName) parameters.get(
                    METHOD_NAME_GET_PROJECT_BY_NAME);
        }
        synchronized (params) {
            params.setProjectName(projectName);
            try {
                response = wsStub.getProjectByName(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }

        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getProjectVersionsByProjectId(long)
     */
    @Override
    public WSProjectVersion[] getProjectVersionsByProjectId(long projectId) throws WSException {
        GetProjectVersionsByProjectIdResponse response;
        GetProjectVersionsByProjectId params;
        if (!parameters.containsKey(METHOD_NAME_GET_PROJECT_VERSIONS_BY_PROJECT_ID)) {
            params = new GetProjectVersionsByProjectId();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_PROJECT_VERSIONS_BY_PROJECT_ID, params);
        } else {
            params = (GetProjectVersionsByProjectId) parameters.get(
                    METHOD_NAME_GET_PROJECT_VERSIONS_BY_PROJECT_ID);
        }
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.getProjectVersionsByProjectId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }

        return (WSProjectVersion[]) normalizeWSArrayResult(response.get_return());

    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getProjectVersionsByIds(long[])
     */
    @Override
    public WSProjectVersion[] getProjectVersionsByIds(long[] projectVersionsIds)
            throws WSException {
        if (!normalizeWSArrayParameter(projectVersionsIds)) return EMPTY_ARRAY_PROJECT_VERSIONS;
        GetProjectVersionsByIdsResponse response;
        GetProjectVersionsByIds params;
        if (!parameters.containsKey(METHOD_NAME_GET_PROJECT_VERSIONS_BY_IDS)) {
            params = new GetProjectVersionsByIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_PROJECT_VERSIONS_BY_IDS, params);
        } else {
            params = (GetProjectVersionsByIds) parameters.get(
                    METHOD_NAME_GET_PROJECT_VERSIONS_BY_IDS);
        }
        synchronized (params) {
            params.setProjectVersionsIds(projectVersionsIds);
            try {
                response = wsStub.getProjectVersionsByIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return (WSProjectVersion[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getLastProjectVersions(long[])
     */
    @Override
    public WSProjectVersion[] getLastProjectVersions(long[] projectsIds) throws WSException {
        if (!normalizeWSArrayParameter(projectsIds)) return EMPTY_ARRAY_PROJECT_VERSIONS;
        GetLastProjectVersionsResponse response;
        GetLastProjectVersions params;
        if (!parameters.containsKey(METHOD_NAME_GET_LAST_PROJECT_VERSIONS)) {
            params = new GetLastProjectVersions();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_LAST_PROJECT_VERSIONS, params);
        } else {
            params = (GetLastProjectVersions) parameters.get(
                    METHOD_NAME_GET_LAST_PROJECT_VERSIONS);
        }
        synchronized (params) {
            params.setProjectsIds(projectsIds);
            try {
                response = wsStub.getLastProjectVersions(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return (WSProjectVersion[]) normalizeWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getProjectsByIds(long[])
     */
    @Override
    public WSStoredProject[] getProjectsByIds(long[] projectsIds) throws WSException {
        if (!normalizeWSArrayParameter(projectsIds)) return EMPTY_ARRAY_STORED_PROJECTS; 
        GetProjectsByIdsResponse response;
        GetProjectsByIds params;
        if (!parameters.containsKey(METHOD_NAME_GET_PROJECTS_BY_IDS)) {
            params = new GetProjectsByIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_PROJECTS_BY_IDS, params);
        } else {
            params = (GetProjectsByIds) parameters.get(
                    METHOD_NAME_GET_PROJECTS_BY_IDS);
        }
        synchronized (params) {
            params.setProjectsIds(projectsIds);
            try {
                response = wsStub.getProjectsByIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }

        return (WSStoredProject[]) normalizeWSArrayResult(response.get_return());

    }

}

//vi: ai nosi sw=4 ts=4 expandtab
