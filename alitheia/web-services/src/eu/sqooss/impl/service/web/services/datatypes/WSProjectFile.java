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
import eu.sqooss.service.db.ProjectFile;

/**
 * This class wraps the <code>eu.sqooss.service.db.ProjectFile</code>
 */
public class WSProjectFile {
    
    private long id;
    private long projectVersionId;
    private long directoryId;
    private long toDirectoryId;
    private String shortName;
    private String fileName;
    private String status;
    private boolean isDirectory;
    
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
     * @return the projectVersionId
     */
    public long getProjectVersionId() {
        return projectVersionId;
    }
    
    /**
     * @param projectVersionId the projectVersionId to set
     */
    public void setProjectVersionId(long projectVersionId) {
        this.projectVersionId = projectVersionId;
    }
    
    /**
     * @return the directoryId
     */
    public long getDirectoryId() {
        return directoryId;
    }
    
    /**
     * @param directoryId the directoryId to set
     */
    public void setDirectoryId(long directoryId) {
        this.directoryId = directoryId;
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * @return the isDirectory
     */
    public boolean isDirectory() {
        return isDirectory;
    }
    
    /**
     * @param isDirectory the isDirectory to set
     */
    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
    
    /**
     * The method creates a new <code>WSProjectFile</code> object
     * from the existent DAO object.
     * The method doesn't care of the db session. 
     * 
     * @param projectFile - DAO project file object
     * 
     * @return The new <code>WSProjectFile</code> object
     */
    public static WSProjectFile getInstance(ProjectFile projectFile) {
        if (projectFile == null) return null;
        try {
            WSProjectFile result = new WSProjectFile();
            result.setId(projectFile.getId());
            result.setDirectoryId(projectFile.getDir().getId());
            result.setDirectory(projectFile.getIsDirectory());
            result.setShortName(projectFile.getName());
            result.setFileName(projectFile.getFileName());
            result.setProjectVersionId(projectFile.getProjectVersion().getId());
            result.setStatus(projectFile.getStatus());
            if (projectFile.getIsDirectory()) {
                Directory dir = projectFile.toDirectory();
                if (dir != null)
                    result.setToDirectoryId(dir.getId());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * The method returns an array containing
     * all of the elements in the project files list.
     * The list argument should contain DAO
     * <code>ProjectFile</code> objects.
     * The method doesn't care of the db session.
     *  
     * @param projectFiles - the project files list;
     * the elements should be <code>ProjectFile</code> objects  
     * 
     * @return - an array with <code>WSProjectFile</code> objects;
     * if the list is null, contains different object type
     * or the DAO can't be wrapped then the array is null
     */
    public static WSProjectFile[] asArray(List<?> projectFiles) {
        WSProjectFile[] result = null;
        if (projectFiles != null) {
            result = new WSProjectFile[projectFiles.size()];
            ProjectFile currentProjectFile;
            WSProjectFile currentWSProjectFile;
            for (int i = 0; i < result.length; i++) {
                try {
                    currentProjectFile = (ProjectFile) projectFiles.get(i);
                } catch (ClassCastException e) {
                    return null;
                }
                currentWSProjectFile = WSProjectFile.getInstance(currentProjectFile);
                if (currentWSProjectFile == null) return null;
                result[i] = currentWSProjectFile;
            }
        }
        return result;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public long getToDirectoryId() {
        return toDirectoryId;
    }

    public void setToDirectoryId(long toDirectoryId) {
        this.toDirectoryId = toDirectoryId;
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
