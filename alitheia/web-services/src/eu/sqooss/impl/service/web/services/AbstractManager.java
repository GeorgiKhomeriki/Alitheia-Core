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

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.User;

public abstract class AbstractManager {
    
    private static final String ATTRIBUTE_USER_NAME = "name";  
    
    private DBService db;
    private Map<String, Object> properties;
    private Object lockObject = new Object();
    
    protected AbstractManager(DBService db) {
        this.db = db;
        properties = new Hashtable<String, Object>(1);
    }
    
    protected boolean updateUserActivity(String userName) {
        Session session = null;
        try {
            session = db.getSession(this);
            synchronized (lockObject) {
                properties.put(ATTRIBUTE_USER_NAME, userName);
                List<User> users = db.findObjectsByProperties(session, User.class, properties);
                if (users.size() != 0) { //the user name is unique
                    users.get(0).setLastActivity(new Date(System.currentTimeMillis()));
                    return true;
                } else {
                    return false; // the user doesn't exist
                }
            }
        } finally {
            if (session != null) {
                session.flush();
                db.returnSession(session);
            }
        }
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
