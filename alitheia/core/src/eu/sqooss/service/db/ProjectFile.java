/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Paul J. Adams <paul.adams@siriusit.co.uk>
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

package eu.sqooss.service.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.sqooss.impl.service.CoreActivator;
import eu.sqooss.service.db.DAObject;

public class ProjectFile extends DAObject{
    private String name;
    private ProjectVersion projectVersion;
    private String status;
    private Boolean isDirectory;
    private Directory dir;


    public ProjectFile() {
        // Nothing to see here
        isDirectory = false; //By default, all entries are files
    }

    public ProjectFile(ProjectVersion pv) {
        this.projectVersion = pv;
        isDirectory = false; //By default, all entries are files
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProjectVersion(ProjectVersion projectVersion ) {
        this.projectVersion = projectVersion;
    }

    public ProjectVersion getProjectVersion() {
        return projectVersion;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public Directory getDir() {
        return dir;
    }

    public void setDir(Directory dir) {
        this.dir = dir;
    }
    
    /**
     * Returns the full path to the file, relative to the repository root
     * @return 
     */
    public String getFileName() {
        String result = dir.getPath();
        if (!result.endsWith("/"))
            result += "/";
        result += name;
        return result;
    }
    
    /**
     * Get the previous entry for the provided ProjectFile
     * @param pf
     * @return The previous file revision, or null if the file is not found 
     * or if the file was added in the provided revision 
     */
    public static ProjectFile getPreviousFileVersion(ProjectFile pf) {
        DBService dbs = CoreActivator.getDBService();
        
        //No need to query if a file was just added
        if (pf.getStatus() == "ADDED") 
            return null;
        
        String paramFile = "paramFile"; 
        String paramVersion = "paramVersion"; 
        String paramDir = "paramDir";
        
        String query = "select pf from ProjectVersion pv, ProjectFile pf " +
        		"where pv.timestamp in (" +
        		"select max(pv2.timestamp) " +
        		"from ProjectVersion pv2, ProjectFile pf2 " +
        		"where pv2.timestamp < :" + paramVersion +
        		" and pf2.projectVersion = pv2.id" +
                        " and pf2.dir = :" + paramDir +
        		" and pf2.name = :" + paramFile +
        		" and pv2.project = pv.project )" +
        		"and pf.projectVersion = pv.id and pf.name = :" + paramFile;
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(paramFile, pf.getName());
        parameters.put(paramDir, pf.getDir());
        parameters.put(paramVersion, pf.getProjectVersion().getTimestamp());

        List<?> projectFiles = dbs.doHQL(query, parameters);
        
        if(projectFiles == null || projectFiles.size() == 0) {
            return null;
        }else {
            return (ProjectFile) projectFiles.get(0);
        }
    }
    
    /**
     * Get the file revision that is current to the provided project version.
     * @param pv The project version against which we want the current version
     * @param path The absolute file path (starting with /)
     * @return The ProjectFile instance or null if the project file was deleted before,
     * has not been added till or not found in the provided project version
     */
    public static ProjectFile getLatestVersion(ProjectVersion pv, String path) {
        DBService dbs = CoreActivator.getDBService();
        
        String dir = path.substring(0, path.lastIndexOf('/'));
        String fname = path.substring(path.lastIndexOf('/') + 1);
        
        if (path == null || path.equalsIgnoreCase("")) {
            path = "/"; 
        }
        
        Directory d = Directory.getDirectory(dir, false);
        
        String paramFile = "paramFile"; 
        String paramTS = "paramTS"; 
        String paramDir = "paramDir";
        
        String query = "select pf from ProjectVersion pv, ProjectFile pf " +
                        "where pv.timestamp in (" +
                        "select max(pv2.timestamp) " +
                        "from ProjectVersion pv2, ProjectFile pf2 " +
                        "where pv2.timestamp <= :" + paramTS +
                        " and pf2.projectVersion = pv2.id" +
                        " and pf2.dir = :" + paramDir +
                        " and pf2.name = :" + paramFile +
                        " and pv2.project = pv.project )" +
                        "and pf.projectVersion = pv.id and pf.name = :" + paramFile;
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(paramFile, fname);
        parameters.put(paramDir, d);
        parameters.put(paramTS, pv.getTimestamp());

        List<?> projectFiles = dbs.doHQL(query, parameters);
        
        if(projectFiles == null || projectFiles.size() == 0) {
            return null;
        }else {
            return (ProjectFile) projectFiles.get(0);
        }
    }

    /**
     * Returns the project version's number where this file was deleted.
     * <br/>
     * This method takes into consideration the deletion of parent folders,
     * thus detecting the situation when a file was deleted indirectly by
     * removing a parent folder.
     * <br/>
     * For a project files in a deleted state, this method will return the
     * project version's number of the same file.
     * 
     * @param pf the project's file
     * 
     * @return The project version's number where this file was deleted,
     *   or <code>null</code> if this file still exist.
     */
    public static Long getDeletionVersion(ProjectFile pf) {
        DBService db = CoreActivator.getDBService();

        // Skip files which are in a "DELETED" state
        if (pf.status.equals("DELETED")) {
            return pf.getProjectVersion().getVersion();
        }

        // Keep the deletion version
        Long deletionVersion = null;

        // Retrieve the version of the given project file
        long fileVersion = pf.getProjectVersion().getVersion();

        // Get all project files in state "DELETED" that match the given
        // file's name and folder
        HashMap<String,Object> props = new HashMap<String,Object>();
        props.put("name", pf.getName());
        props.put("dir", pf.getDir());
        props.put("status", new String("DELETED"));
        List<ProjectFile> deletions =
            db.findObjectsByProperties(ProjectFile.class, props);
        // Check if this file was deleted at all
        if ((deletions != null) && (deletions.size() > 0)) {
            for (ProjectFile nextDeletion : deletions) {
                // Skip deletion matches that are not in the same project
                if (nextDeletion.getProjectVersion().getProject().getId()
                        != pf.getProjectVersion().getProject().getId())
                    continue;
                // Skip deletion matches that are older than the given file
                long nextDeletionVersion =
                    nextDeletion.getProjectVersion().getVersion();
                if (nextDeletionVersion <= fileVersion)
                    continue;
                // Check if this deletion is a closer match
                if ((deletionVersion == null)
                        || (deletionVersion > nextDeletionVersion)) {
                    deletionVersion = 
                        nextDeletionVersion;
                }
            }
        }

        // Take into consideration the deletion version of the parent folder
        ProjectFile parentFolder = getParentFolder(pf);
        if (parentFolder != null) {
            Long parentDeletionVersion = getDeletionVersion(parentFolder);
            if (parentDeletionVersion != null) {
                // Check if the parent folder was deleted later on
                if ((deletionVersion != null)
                    && (parentDeletionVersion.longValue()
                            > deletionVersion.longValue())) {
                    return deletionVersion;
                }
                return parentDeletionVersion;
            }
        }

        // Return the project's version where this file was deleted
        return deletionVersion;
    }

    /**
     * Gets the parent folder of the given project file.
     * 
     * @param pf the project file
     * 
     * @return The <code>ProjectFile</code> DAO of the parent folder,
     *   or <code>null</code> if the given file is located in the project's
     *   root folder (<i>or the given file is the root folder</i> ).
     */
    public static ProjectFile getParentFolder(ProjectFile pf) {
        DBService db = CoreActivator.getDBService();

        // Get the file's folder
        String filePath = pf.getDir().getPath();

        // Proceed only if this file is not the project's root folder
        if (filePath.matches("^/+$") == false) {
            // Split the folder into folder's name and folder's path
            String dirPath =
                filePath.substring(0, filePath.lastIndexOf('/') + 1);
            if (dirPath.matches(".+/$")) {
                // Remove the trailing path separator from the folder's path
                dirPath = dirPath.substring(0, dirPath.lastIndexOf('/'));
            }
            String dirName =
                filePath.substring(filePath.lastIndexOf('/') + 1);
            // Retrieve the Directory DAO of the extracted folder's path
            HashMap<String,Object> props = new HashMap<String,Object>();
            props.put("path", dirPath);
            List<Directory> dirs =
                db.findObjectsByProperties(Directory.class, props);
            // Retrieve the ProjectFile DAOs of all folders that can be a
            // parent of the given project file.
            props.clear();
            props.put("name", dirName);
            props.put("dir", dirs.get(0));
            props.put("status", new String("ADDED"));
            List<ProjectFile> folders =
                db.findObjectsByProperties(ProjectFile.class, props);
            // Match until the "real" parent folder is found
            if ((folders != null) && (folders.size() > 0)) {
                // Retrieve the version of the given project file
                long fileVersion = pf.getProjectVersion().getVersion();
                // Keep the matched folder's DAO
                ProjectFile fileFolder = null; 
                for (ProjectFile nextFolder : folders) {
                    // Skip folder matches that are not in the same project
                    if (nextFolder.getProjectVersion().getProject().getId()
                            != pf.getProjectVersion().getProject().getId())
                        continue;
                    // Skip folder matches that are newer than the given file
                    long nextFolderVersion =
                        nextFolder.getProjectVersion().getVersion();
                    if (nextFolderVersion > fileVersion)
                        continue;
                    // Check if this folder is a closer match
                    if ((fileFolder == null)
                            || (fileFolder.projectVersion.getVersion()
                                    < nextFolderVersion)) {
                        fileFolder = nextFolder;
                    }
                }
                // Return the parent folder's DAO
                return fileFolder;
            }
        }

        return null;
    }
}

//vi: ai nosi sw=4 ts=4 expandtab

