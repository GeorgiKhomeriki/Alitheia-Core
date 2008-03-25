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

import eu.sqooss.scl.accessor.WSUserAccessor;
import eu.sqooss.ws.client.WsStub;
import eu.sqooss.ws.client.datatypes.WSUser;
import eu.sqooss.ws.client.ws.DeleteUser;
import eu.sqooss.ws.client.ws.DisplayUser;
import eu.sqooss.ws.client.ws.DisplayUserResponse;
import eu.sqooss.ws.client.ws.ModifyUser;
import eu.sqooss.ws.client.ws.SubmitUser;
import eu.sqooss.ws.client.ws.SubmitUserResponse;

class WSUserAccessorImpl extends WSUserAccessor {
    
    private static final String METHOD_NAME_SUBMIT_USER  = "submitUser";
    
    private static final String METHOD_NAME_DISPLAY_USER = "displayUser";
    
    private static final String METHOD_NAME_MODIFY_USER  = "modifyUser";
    
    private static final String METHOD_NAME_DELETE_USER  = "deleteUser";
    
    private Map<String, Object> parameters;
    private String userName;
    private String password;
    private WsStub wsStub;
    
    public WSUserAccessorImpl(String userName, String password, String webServiceUrl) throws WSException {
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
     * @see eu.sqooss.scl.accessor.WSUserAccessor#submitUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public WSUser submitUser(String newUserName, String newNames, String newPassword,
            String newUserClass, String newOtherInfo) throws WSException {
        SubmitUserResponse response;
        SubmitUser params = (SubmitUser) parameters.get(METHOD_NAME_SUBMIT_USER);
        synchronized (params) {
            params.setNewUserName(newUserName);
            params.setNewNames(newNames);
            params.setNewPassword(newPassword);
            params.setNewUserClass(newUserClass);
            params.setNewOtherInfo(newOtherInfo);

            try {
                response = wsStub.submitUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSUser) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#displayUser(long)
     */
    @Override
    public WSUser displayUser(long userId) throws WSException {
        DisplayUserResponse response;
        DisplayUser params = (DisplayUser) parameters.get(METHOD_NAME_DISPLAY_USER);
        synchronized (params) {
            params.setUserId(userId);
            try {
                response = wsStub.displayUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSUser) parseWSResult(response.get_return());
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#modifyUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void modifyUser(String userName, String newNames, String newPassword,
            String newUserClass, String newOtherInfo) throws WSException {
        ModifyUser params = (ModifyUser) parameters.get(METHOD_NAME_MODIFY_USER);
        synchronized (params) {
            params.setUserName(userName);
            params.setNewNames(newNames);
            params.setNewPassword(newPassword);
            params.setNewUserClass(newUserClass);
            params.setNewOtherInfo(newOtherInfo);
            try {
                wsStub.modifyUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
    }
    
    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#deleteUser(long)
     */
    @Override
    public void deleteUser(long userId) throws WSException {
        DeleteUser params = (DeleteUser) parameters.get(METHOD_NAME_DELETE_USER);
        synchronized (params) {
            params.setUserId(userId);
            try {
                wsStub.deleteUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
    }
    
    private void initParameters() {
        parameters = new Hashtable<String, Object>();
        
        SubmitUser submitUser = new SubmitUser();
        submitUser.setPasswordForAccess(password);
        submitUser.setUserNameForAccess(userName);
        parameters.put(METHOD_NAME_SUBMIT_USER, submitUser);
        
        DisplayUser displayUser = new DisplayUser();
        displayUser.setPasswordForAccess(password);
        displayUser.setUserNameForAccess(userName);
        parameters.put(METHOD_NAME_DISPLAY_USER, displayUser);
        
        ModifyUser modifyUser = new ModifyUser();
        modifyUser.setPasswordForAccess(password);
        modifyUser.setUserNameForAccess(userName);
        parameters.put(METHOD_NAME_MODIFY_USER, modifyUser);
        
        DeleteUser deleteUser = new DeleteUser();
        deleteUser.setPasswordForAccess(password);
        deleteUser.setUserNameForAccess(userName);
        parameters.put(METHOD_NAME_DELETE_USER, deleteUser);
        
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
