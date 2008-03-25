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
import eu.sqooss.ws.client.datatypes.WSProjectFile;
import eu.sqooss.ws.client.datatypes.WSProjectVersion;
import eu.sqooss.ws.client.datatypes.WSStoredProject;
import eu.sqooss.ws.client.ws.EvaluatedProjectsList;
import eu.sqooss.ws.client.ws.EvaluatedProjectsListResponse;
import eu.sqooss.ws.client.ws.GetFileList4ProjectVersion;
import eu.sqooss.ws.client.ws.GetFileList4ProjectVersionResponse;
import eu.sqooss.ws.client.ws.GetFilesNumber4ProjectVersion;
import eu.sqooss.ws.client.ws.GetFilesNumber4ProjectVersionResponse;
import eu.sqooss.ws.client.ws.RequestEvaluation4Project;
import eu.sqooss.ws.client.ws.RequestEvaluation4ProjectResponse;
import eu.sqooss.ws.client.ws.RetrieveFileList;
import eu.sqooss.ws.client.ws.RetrieveFileListResponse;
import eu.sqooss.ws.client.ws.RetrieveProjectId;
import eu.sqooss.ws.client.ws.RetrieveProjectIdResponse;
import eu.sqooss.ws.client.ws.RetrieveStoredProject;
import eu.sqooss.ws.client.ws.RetrieveStoredProjectResponse;
import eu.sqooss.ws.client.ws.RetrieveStoredProjectVersions;
import eu.sqooss.ws.client.ws.RetrieveStoredProjectVersionsResponse;
import eu.sqooss.ws.client.ws.StoredProjectsList;
import eu.sqooss.ws.client.ws.StoredProjectsListResponse;

class WSProjectAccessorImpl extends WSProjectAccessor {

    private static final String METHOD_NAME_EVALUATED_PROJECTS_LIST     = "evaluatedProjectsList";
    
    private static final String METHOD_NAME_STORED_PROJECTS_LIST        = "storedProjectsList";
    
    private static final String METHOD_NAME_RETRIEVE_FILE_LIST          = "retrieveFileList";
    
    private static final String METHOD_NAME_REQUEST_EVALUATION_4_PROJECT = "requestEvaluation4Project";
    
    private static final String METHOD_NAME_RETRIEVE_PROJECT_ID          = "retrieveProjectId";
    
    private static final String METHOD_NAME_RETRIEVE_STORED_PROJECT_VERSIONS     = "retrieveStoredProjectVersions";
    
    private static final String METHOD_NAME_RETRIEVE_STORED_PROJECT              = "retrieveStoredProject";
    
    private static final String METHOD_NAME_GET_FILES_NUMBER_4_PROJECT_VERSION   = "getFilesNumber4ProjectVersion";
    
    private static final String METHOD_NAME_GET_FILE_LIST_4_PROJECT_VERSION      = "getFileList4ProjectVersion";
    
    private Map<String, Object> parameters;
    private String userName;
    private String password;
    private WsStub wsStub;
    
    public WSProjectAccessorImpl(String userName, String password, String webServiceUrl) throws WSException {
        this.userName = userName;
        this.password = password;
        try {
            this.wsStub = new WsStub(webServiceUrl);
        } catch (AxisFault af) {
            throw new WSException(af);
        }
        initParameters();
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#evaluatedProjectsList()
     */
    @Override
    public WSStoredProject[] evaluatedProjectsList() throws WSException {
        EvaluatedProjectsListResponse response; 
        EvaluatedProjectsList params = (EvaluatedProjectsList) parameters.get(
                METHOD_NAME_EVALUATED_PROJECTS_LIST);
        synchronized (params) {
            try {
                response = wsStub.evaluatedProjectsList(params);
            } catch (RemoteException e) {
                throw new WSException(e);
            }
        }
        return (WSStoredProject[]) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#storedProjectsList()
     */
    @Override
    public WSStoredProject[] storedProjectsList() throws WSException {
        StoredProjectsListResponse response; 
        StoredProjectsList params = (StoredProjectsList) parameters.get(
                METHOD_NAME_STORED_PROJECTS_LIST);
        synchronized (params) {
            try {
                response = wsStub.storedProjectsList(params);
            } catch (RemoteException e) {
                throw new WSException(e);
            }
        }
        return (WSStoredProject[]) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#retrieveFileList(long)
     */
    @Override
    public WSProjectFile[] retrieveFileList(long projectId) throws WSException {
        RetrieveFileListResponse response;
        RetrieveFileList params = (RetrieveFileList) parameters.get(
                METHOD_NAME_RETRIEVE_FILE_LIST);
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.retrieveFileList(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSProjectFile[]) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFileList4ProjectVersion(long)
     */
    @Override
    public WSProjectFile[] getFileList4ProjectVersion(long projectVersionId) throws WSException {
        GetFileList4ProjectVersionResponse response;
        GetFileList4ProjectVersion params = (GetFileList4ProjectVersion) parameters.get(
                METHOD_NAME_GET_FILE_LIST_4_PROJECT_VERSION);
        synchronized (params) {
            params.setProjectVersionId(projectVersionId);
            try {
                response = wsStub.getFileList4ProjectVersion(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSProjectFile[]) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#getFilesNumber4ProjectVersion(long)
     */
    @Override
    public long getFilesNumber4ProjectVersion(long projectVersionId) throws WSException {
        GetFilesNumber4ProjectVersionResponse response;
        GetFilesNumber4ProjectVersion params = (GetFilesNumber4ProjectVersion) parameters.get(
                METHOD_NAME_GET_FILES_NUMBER_4_PROJECT_VERSION);
        synchronized (params) {
            params.setProjectVersionId(projectVersionId);
            try {
                response = wsStub.getFilesNumber4ProjectVersion(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return response.get_return();
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#requestEvaluation4Project(java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public WSStoredProject requestEvaluation4Project(String projectName, long projectVersion,
            String srcRepositoryLocation, String mailingListLocation,
            String BTSLocation, String userEmailAddress, String website) throws WSException {
        RequestEvaluation4ProjectResponse response;
        RequestEvaluation4Project params = (RequestEvaluation4Project) parameters.get(
                METHOD_NAME_REQUEST_EVALUATION_4_PROJECT);
        synchronized (params) {
            params.setProjectName(projectName);
            params.setProjectVersion(projectVersion);
            params.setSrcRepositoryLocation(srcRepositoryLocation);
            params.setMailingListLocation(mailingListLocation);
            params.setBTSLocation(BTSLocation);
            params.setUserEmailAddress(userEmailAddress);
            params.setWebsite(website);
            try {
                response = wsStub.requestEvaluation4Project(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return response.get_return();
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#retrieveProjectId(java.lang.String)
     */
    @Override
    public long retrieveProjectId(String projectName) throws WSException {
        RetrieveProjectIdResponse response;
        RetrieveProjectId params = (RetrieveProjectId) parameters.get(
                METHOD_NAME_RETRIEVE_PROJECT_ID);
        synchronized (params) {
            params.setProjectName(projectName);
            try {
                response = wsStub.retrieveProjectId(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return response.get_return();
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#retrieveStoredProjectVersions(long)
     */
    @Override
    public WSProjectVersion[] retrieveStoredProjectVersions(long projectId) throws WSException {
        RetrieveStoredProjectVersionsResponse response;
        RetrieveStoredProjectVersions params = (RetrieveStoredProjectVersions) parameters.get(
                METHOD_NAME_RETRIEVE_STORED_PROJECT_VERSIONS);
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.retrieveStoredProjectVersions(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return (WSProjectVersion[]) parseWSResult(response.get_return());
        
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSProjectAccessor#retrieveStoredProject(long)
     */
    @Override
    public WSStoredProject retrieveStoredProject(long projectId) throws WSException {
        RetrieveStoredProjectResponse response;
        RetrieveStoredProject params = (RetrieveStoredProject) parameters.get(
                METHOD_NAME_RETRIEVE_STORED_PROJECT);
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.retrieveStoredProject(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        
        return (WSStoredProject) parseWSResult(response.get_return());
        
    }
    
    private void initParameters() {
        parameters = new Hashtable<String, Object>();
        
        EvaluatedProjectsList evaluatedProjectsListParams = new EvaluatedProjectsList();
        evaluatedProjectsListParams.setPassword(password);
        evaluatedProjectsListParams.setUserName(userName);
        parameters.put(METHOD_NAME_EVALUATED_PROJECTS_LIST, evaluatedProjectsListParams);
 
        StoredProjectsList storedProjectsListParams = new StoredProjectsList();
        storedProjectsListParams.setPassword(password);
        storedProjectsListParams.setUserName(userName);
        parameters.put(METHOD_NAME_STORED_PROJECTS_LIST, storedProjectsListParams);
        
        RetrieveFileList retrieveFileListParams = new RetrieveFileList();
        retrieveFileListParams.setPassword(password);
        retrieveFileListParams.setUserName(userName);
        parameters.put(METHOD_NAME_RETRIEVE_FILE_LIST, retrieveFileListParams);
        
        RequestEvaluation4Project requestEvaluation4ProjectParams = new RequestEvaluation4Project();
        requestEvaluation4ProjectParams.setPassword(password);
        requestEvaluation4ProjectParams.setUserName(userName);
        parameters.put(METHOD_NAME_REQUEST_EVALUATION_4_PROJECT, requestEvaluation4ProjectParams);
        
        RetrieveProjectId retrieveProjectIdParam = new RetrieveProjectId();
        retrieveProjectIdParam.setPasswrod(password);
        retrieveProjectIdParam.setUserName(userName);
        parameters.put(METHOD_NAME_RETRIEVE_PROJECT_ID, retrieveProjectIdParam);
        
        RetrieveStoredProjectVersions retrieveStoredProjectVersionsParam =
            new RetrieveStoredProjectVersions();
        retrieveStoredProjectVersionsParam.setPassword(password);
        retrieveStoredProjectVersionsParam.setUserName(userName);
        parameters.put(METHOD_NAME_RETRIEVE_STORED_PROJECT_VERSIONS,
                retrieveStoredProjectVersionsParam);
        
        RetrieveStoredProject retrieveStoredProjectParam = new RetrieveStoredProject();
        retrieveStoredProjectParam.setPassword(password);
        retrieveStoredProjectParam.setUserName(userName);
        parameters.put(METHOD_NAME_RETRIEVE_STORED_PROJECT,
                retrieveStoredProjectParam);
        
        GetFilesNumber4ProjectVersion getFilesNumber4ProjectVersionParam = new GetFilesNumber4ProjectVersion();
        getFilesNumber4ProjectVersionParam.setPassword(password);
        getFilesNumber4ProjectVersionParam.setUserName(userName);
        parameters.put(METHOD_NAME_GET_FILES_NUMBER_4_PROJECT_VERSION,
                getFilesNumber4ProjectVersionParam);
        
        GetFileList4ProjectVersion getFileList4ProjectVersionParam = new GetFileList4ProjectVersion();
        getFileList4ProjectVersionParam.setPassword(password);
        getFileList4ProjectVersionParam.setUserName(userName);
        parameters.put(METHOD_NAME_GET_FILE_LIST_4_PROJECT_VERSION,
                getFileList4ProjectVersionParam);
        
        
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
