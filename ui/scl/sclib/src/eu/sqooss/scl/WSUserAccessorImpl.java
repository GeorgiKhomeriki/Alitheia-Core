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
import eu.sqooss.ws.client.ws.CreatePendingUser;
import eu.sqooss.ws.client.ws.CreatePendingUserResponse;
import eu.sqooss.ws.client.ws.CreateUser;
import eu.sqooss.ws.client.ws.CreateUserResponse;
import eu.sqooss.ws.client.ws.DeleteUserById;
import eu.sqooss.ws.client.ws.DeleteUserByIdResponse;
import eu.sqooss.ws.client.ws.GetUserById;
import eu.sqooss.ws.client.ws.GetUserByIdResponse;
import eu.sqooss.ws.client.ws.GetUserByName;
import eu.sqooss.ws.client.ws.GetUserByNameResponse;
import eu.sqooss.ws.client.ws.GetUserMessageOfTheDay;
import eu.sqooss.ws.client.ws.GetUserMessageOfTheDayResponse;
import eu.sqooss.ws.client.ws.ModifyUser;
import eu.sqooss.ws.client.ws.ModifyUserResponse;

class WSUserAccessorImpl extends WSUserAccessor {

    private static final String METHOD_NAME_CREATE_USER          = "createUser";

    private static final String METHOD_NAME_CREATE_PENDING_USER  = "createPendingUser";

    private static final String METHOD_NAME_GET_USER_BY_ID       = "getUserById";

    private static final String METHOD_NAME_GET_USER_BY_NAME     = "getUserByName";

    private static final String METHOD_NAME_MODIFY_USER          = "modifyUser";

    private static final String METHOD_NAME_DELETE_USER_BY_ID    = "deleteUserById";

    private static final String METHOD_NAME_GET_USER_MESSAGE_OF_THE_DAY = "getUserMessageOfTheDay";

    private Map<String, Object> parameters;
    private String userName;
    private String password;
    private WsStub wsStub;

    public WSUserAccessorImpl(String userName, String password, String webServiceUrl) throws WSException {
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
     * @see eu.sqooss.scl.accessor.WSUserAccessor#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public WSUser createUser(String newUserName, String newPassword,
            String email) throws WSException {
        CreateUserResponse response;
        CreateUser params;
        if (!parameters.containsKey(METHOD_NAME_CREATE_USER)) {
            params = new CreateUser();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_CREATE_USER, params);
        } else {
            params = (CreateUser) parameters.get(
                    METHOD_NAME_CREATE_USER);
        }
        synchronized (params) {
            params.setNewUserName(newUserName);
            params.setNewPassword(newPassword);
            params.setEmail(email);
            try {
                response = wsStub.createUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSUser) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#createPendingUser(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean createPendingUser(String newUserName, String newPassword,
            String email) throws WSException {
        CreatePendingUserResponse response;
        CreatePendingUser params;
        if (!parameters.containsKey(METHOD_NAME_CREATE_PENDING_USER)) {
            params = new CreatePendingUser();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_CREATE_PENDING_USER, params);
        } else {
            params = (CreatePendingUser) parameters.get(
                    METHOD_NAME_CREATE_PENDING_USER);
        }
        synchronized (params) {
            params.setNewUserName(newUserName);
            params.setNewPassword(newPassword);
            params.setEmail(email);
            try {
                response = wsStub.createPendingUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#getUserById(long)
     */
    @Override
    public WSUser getUserById(long userId) throws WSException {
        GetUserByIdResponse response;
        GetUserById params;
        if (!parameters.containsKey(METHOD_NAME_GET_USER_BY_ID)) {
            params = new GetUserById();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_GET_USER_BY_ID, params);
        } else {
            params = (GetUserById) parameters.get(
                    METHOD_NAME_GET_USER_BY_ID);
        }
        synchronized (params) {
            params.setUserId(userId);
            try {
                response = wsStub.getUserById(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSUser) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#getUserByName(String)
     */
    @Override
    public WSUser getUserByName(String name) throws WSException {
        GetUserByNameResponse response;
        GetUserByName params;
        if (!parameters.containsKey(METHOD_NAME_GET_USER_BY_NAME)) {
            params = new GetUserByName();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_GET_USER_BY_NAME, params);
        } else {
            params = (GetUserByName) parameters.get(
                    METHOD_NAME_GET_USER_BY_NAME);
        }
        synchronized (params) {
            params.setUserName(name);
            try {
                response = wsStub.getUserByName(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSUser) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#modifyUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean modifyUser(String userName, String newPassword,
            String newEmail) throws WSException {
        ModifyUserResponse response;
        ModifyUser params;
        if (!parameters.containsKey(METHOD_NAME_MODIFY_USER)) {
            params = new ModifyUser();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_MODIFY_USER, params);
        } else {
            params = (ModifyUser) parameters.get(
                    METHOD_NAME_MODIFY_USER);
        }
        synchronized (params) {
            params.setUserName(userName);
            params.setNewPassword(newPassword);
            params.setNewEmail(newEmail);
            try {
                response = wsStub.modifyUser(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#deleteUserById(long)
     */
    @Override
    public boolean deleteUserById(long userId) throws WSException {
        DeleteUserByIdResponse response;
        DeleteUserById params;
        if (!parameters.containsKey(METHOD_NAME_DELETE_USER_BY_ID)) {
            params = new DeleteUserById();
            params.setPasswordForAccess(password);
            params.setUserNameForAccess(userName);
            parameters.put(METHOD_NAME_DELETE_USER_BY_ID, params);
        } else {
            params = (DeleteUserById) parameters.get(
                    METHOD_NAME_DELETE_USER_BY_ID);
        }
        synchronized (params) {
            params.setUserId(userId);
            try {
                response = wsStub.deleteUserById(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return response.get_return();
    }

    /**
     * @see eu.sqooss.scl.accessor.WSUserAccessor#getUserMessageOfTheDay(java.lang.String)
     */
    public String getUserMessageOfTheDay(String userName) throws WSException {
        GetUserMessageOfTheDayResponse response;
        GetUserMessageOfTheDay params;
        if (!parameters.containsKey(METHOD_NAME_GET_USER_MESSAGE_OF_THE_DAY)) {
            params = new GetUserMessageOfTheDay();
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_USER_MESSAGE_OF_THE_DAY, params);
        } else {
            params = (GetUserMessageOfTheDay) parameters.get(
                    METHOD_NAME_GET_USER_MESSAGE_OF_THE_DAY);
        }
        synchronized (params) {
            params.setUserName(userName);
            try {
                response = wsStub.getUserMessageOfTheDay(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return response.get_return();
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
