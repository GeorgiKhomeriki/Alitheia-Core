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

import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.User;
import eu.sqooss.service.security.SecurityManager;
import eu.sqooss.impl.service.web.services.datatypes.WSUser;
import eu.sqooss.impl.service.web.services.utils.SecurityWrapper;

public class UserManager extends AbstractManager {
    
    private SecurityWrapper security;
    private eu.sqooss.service.security.UserManager userManager;
    
    public UserManager(SecurityManager securityManager, DBService db) {
        super(db);
        this.security = new SecurityWrapper(securityManager);
        this.userManager = securityManager.getUserManager();
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#createUser(String, String, String, String, String)
     */
    public WSUser createUser(String userNameForAccess, String passwordForAccess,
            String newUserName, String newPassword, String email) {
        
        security.checkUserWriteAccess(userNameForAccess, passwordForAccess, -1, null);
        
        super.updateUserActivity(userNameForAccess);
        
        db.startDBSession();
        User newUser = userManager.createUser(newUserName, newPassword, email);
        if (newUser != null) {
            WSUser wsu = new WSUser(newUser);
            db.commitDBSession();
            return wsu;
        } else {
            db.rollbackDBSession();
            return null;
        }
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#createPendingUser(String, String, String, String, String)
     */
    public boolean createPendingUser(String userNameForAccess, String passwordForAccess,
            String newUserName, String newPassword, String email) {
        
        security.checkUserWriteAccess(userNameForAccess, passwordForAccess, -1, null);
        
        super.updateUserActivity(userNameForAccess);
        
        db.startDBSession();
        boolean ok = userManager.createPendingUser(newUserName, newPassword, email);
        if (ok) {
            db.commitDBSession();
        } else {
            db.rollbackDBSession();
        }
        return ok;
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getUserById(String, String, long)
     */
    public WSUser getUserById(String userNameForAccess,
            String passwordForAccess, long userId) {
        
        security.checkUserReadAccess(userNameForAccess,
                passwordForAccess, userId, null);
        
        super.updateUserActivity(userNameForAccess);
        
        db.startDBSession();
        User user = userManager.getUser(userId); 
        if (user != null) {
            WSUser wsu = new WSUser(user);
            db.commitDBSession();
            return wsu;
        } else {
            db.rollbackDBSession();
            return null;
        }
        
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#modifyUser(String, String, String, String, String)
     */
    public boolean modifyUser(String userNameForAccess, String passwordForAccess,
            String userName, String newPassword, String newEmail) {
        
        security.checkUserWriteAccess(userNameForAccess, passwordForAccess,
                -1, userName);
        
        super.updateUserActivity(userNameForAccess);
        
        db.startDBSession();
        boolean ok = userManager.modifyUser(userName, newPassword, newEmail);
        if (ok) {
            db.commitDBSession();
        } else {
            db.rollbackDBSession();
        }
        return ok;
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#deleteUserById(String, String, long)
     */
    public boolean deleteUserById(String userNameForAccess, String passwordForAccess, long userId) {
        
        security.checkUserWriteAccess(userNameForAccess, passwordForAccess, userId, null);
        
        super.updateUserActivity(userNameForAccess);
        
        db.startDBSession();
        boolean ok = userManager.deleteUser(userId);
        if (ok) {
            db.commitDBSession();
        } else {
            db.rollbackDBSession();
        }
        return ok;
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getUserByName(String, String, String)
     */
    public WSUser getUserByName(String userNameForAccess,
            String passwordForAccess, String userName) {
        
        security.checkUserReadAccess(userNameForAccess, passwordForAccess, -1, userName);
        
        super.updateUserActivity(userNameForAccess);

        db.startDBSession();
        User user = userManager.getUser(userName);
        if (user != null) {
            WSUser wsu = new WSUser(user);
            db.commitDBSession();
            return wsu;
        } else {
            db.rollbackDBSession();
            return null;
        }
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
