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

package eu.sqooss.impl.service.web.services.datatypes;

import java.util.List;

import eu.sqooss.service.db.Directory;

public class WSDirectory {
    
    private long id;
    private String path;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * The method creates a new <code>WSDirectory</code> object
     * from the existent DAO object.
     * The method doesn't care of the db session. 
     * 
     * @param directory - DAO directory object
     * 
     * @return The new <code>WSDirectory</code> object
     */
    public static WSDirectory getInstance(Directory directory) {
        if (directory == null) return null;
        try {
            WSDirectory wsDirectory = new WSDirectory();
            wsDirectory.setId(directory.getId());
            wsDirectory.setPath(directory.getPath());
            return wsDirectory;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * The method returns an array containing
     * all of the elements in the directories list.
     * The list argument should contain DAO
     * <code>Directory</code> objects.
     *  
     * @param directories - the directories list;
     * the elements should be <code>Directory</code> objects  
     * 
     * @return - an array with <code>WSDirectory</code> objects;
     * if the list is null, empty or contains different object type
     * then the array is null
     */
    public static WSDirectory[] asArray(List<?> directories) {
        WSDirectory[] result = null;
        if ((directories != null) && (!directories.isEmpty()) &&
                (directories.get(0) instanceof Directory)) {
            result = new WSDirectory[directories.size()];
            Directory currentElem;
            for (int i = 0; i < result.length; i++) {
                try {
                    currentElem = (Directory) directories.get(i);
                } catch (ClassCastException e) {
                    return null;
                }
                result[i] = WSDirectory.getInstance(currentElem);
            }
        }
        return result;
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
