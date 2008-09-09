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
import java.util.Set;

import eu.sqooss.impl.service.CoreActivator;

import eu.sqooss.service.tds.ProjectRevision;

/**
 * Instances of this class represent the data about a version of a
 * project as stored in the database
 */
public class ProjectVersion extends DAObject {
    /**
     * The project to which this object relates
     */
    private StoredProject project;

    /**
     * The version number of the project to which this object relates
     */
    private long version;

    /**
     * The date/time at which this version occurs
     */
    private long timestamp;

    /**
     * The developer causing this revision of the project
     */
    private Developer committer;

    /**
     * The commit message provided by the developer as the revision was made
     */
    private String commitMsg;

    private String properties;
    
    /**
     * The added/modified/deleted files for that version
     */
    private Set<ProjectFile> versionFiles;
    /**
     * The complete set of files for that version (ie. the state of the project at that version).
     * Note that the files contained in this set may come from a previous version, if they haven't
     * been modified since.
     */
    private Set<ProjectFile> filesForVersion;
    /**
     * The file groups contained in that version
     */
    private Set<FileGroup> fileGroups;
    /**
     * The set of known tags in this version of the project
     */
    private Set<Tag> tags;
    
    /**
     * The set of known bugs in this version of the projecy
     */
    private Set<Bug> bugs;
    
    //private Set<FileForVersion> 

    /**
     * The set of measurements available for the given version of the project
     */
    private Set<ProjectVersionMeasurement> measurements;

    public ProjectVersion() {
        // Nothing to do
    }
    public ProjectVersion(StoredProject project) {
        this.project = project;
    }

    public StoredProject getProject() {
        return project;
    }

    public void setProject(StoredProject project) {
        this.project = project;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Developer getCommitter() {
        return committer;
    }

    public void setCommitter(Developer committer) {
        this.committer = committer;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    public void setCommitMsg(String commitMsg) {
        this.commitMsg = commitMsg;
    }
    
    public Set<ProjectFile> getVersionFiles() {
        return versionFiles;
    }
    
    public void setVersionFiles( Set<ProjectFile> versionFiles ) {
        this.versionFiles = versionFiles;
    }

    public Set<ProjectFile> getFilesForVersion() {
        return filesForVersion;
    }
    public void setFilesForVersion(Set<ProjectFile> filesForVersion) {
        this.filesForVersion = filesForVersion;
    }
    public Set<FileGroup> getFileGroups() {
        return fileGroups;
    }
    
    public void setFileGroups(Set<FileGroup> fileGroups) {
        this.fileGroups = fileGroups;
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    public Set<Bug> getBugs() {
        return bugs;
    }
    
    public void setBugs(Set<Bug> bugs) {
        this.bugs = bugs;
    }
    
    public Set<ProjectVersionMeasurement> getMeasurements() {
        return measurements;
    }
    
    public void setMeasurements(Set<ProjectVersionMeasurement> measurements) {
        this.measurements = measurements;
    }

    /**
     * Allow moving backward in version history by finding the most-recent
     * version of this project before the current one, or null if there
     * is no such version.
     * 
     * @return Previous version, or null
     */
    public ProjectVersion getPreviousVersion() {
        DBService dbs = CoreActivator.getDBService();
        
        String paramTS = "version_timestamp"; 
        String paramProject = "project_id";
        
        String query = "from ProjectVersion pv where " +
                        "pv.project.id=:" + paramProject + 
                        " and pv.timestamp in (" +
        		"select max(pv2.timestamp) " +
        		"from ProjectVersion pv2 " +
        		"where pv2.timestamp < :" + paramTS +
        		" and pv2.project = pv.project)";
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(paramTS, this.getTimestamp());
        parameters.put(paramProject, this.getProject().getId());

        List<?> projectVersions = dbs.doHQL(query, parameters);
        
        if(projectVersions == null || projectVersions.size() == 0) {
            return null;
        } else {
            return (ProjectVersion) projectVersions.get(0);
        }
    }
    
    /**
     * Allow moving forward in version history by finding the earliest
     * version of this project later than the current one, or null if there
     * is no such version.
     * 
     * @return Next version, or null
     */
    public ProjectVersion getNextVersion() {
        DBService dbs = CoreActivator.getDBService();
        
        String paramTS = "version_timestamp"; 
        String paramProject = "project_id";
        
        String query = "from ProjectVersion pv where " +
                        "pv.project.id=:" + paramProject + 
                        " and pv.timestamp in (" +
        		"select min(pv2.timestamp) " +
        		"from ProjectVersion pv2 " +
        		"where pv2.timestamp > :" + paramTS +
        		" and pv2.project = pv.project)";
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(paramTS, this.getTimestamp());
        parameters.put(paramProject, this.getProject().getId());

        List<?> projectVersions = dbs.doHQL(query, parameters);
        
        if(projectVersions == null || projectVersions.size() == 0) {
            return null;
        } else {
            return (ProjectVersion) projectVersions.get(0);
        }
    }
    
    public boolean lte(ProjectVersion p) {
        return this.timestamp <= p.getTimestamp();
    }
    
    /**
     * Pseudo-constructor to look up a stored ProjectVersion by project
     * and revision -- this is different from creating a new object
     * through the constructor because it will return null if the combination
     * of project and SVN revision does not exist in the database.
     * 
     * @param project Project to look up
     * @param revision SVN revision (must have a revision number associated)
     * @return ProjectVersion, or null if none found
     */
    public static ProjectVersion getVersionByRevision( StoredProject project, ProjectRevision revision ) {
        if (!revision.hasSVNRevision()) {
            // Can't do the lookup if we don't have a SVN revision number;
            // caller should have resolved other revision specifications 
            // to a number already.
            return null;
            // TODO: possibly log this
        }
        
        DBService dbs = CoreActivator.getDBService();
   
        String paramProjectId = "stored_project_id";
        String paramRevision = "revision_nr";
        String query = "select pv " +
                       "from ProjectVersion pv " +
                       "where pv.project.id=:" + paramProjectId + " and " +
                       "pv.version=:" + paramRevision;

        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(paramProjectId, project.getId());
        parameters.put(paramRevision, revision.getSVNRevision());

        List<?> projectVersions = dbs.doHQL(query, parameters);
        if (projectVersions == null || projectVersions.size() == 0) {
            return null;
        } else {
            return (ProjectVersion) projectVersions.get(0);
        }
    }
    
    /**
     * Gets the number of files in the given version which are in the
     * selected file state.
     * 
     * @param pv the version DAO
     * @param state the file state
     * 
     * @return The number of files in that version and that state.
     */
    public static long getFilesCount(ProjectVersion pv, String state) {
        DBService dbs = CoreActivator.getDBService();
        // Construct the field names
        String parVersionId     = "project_version_id"; 
        String parFileStatus    = "file_status";
        // Construct the query string
        String query = "select count(*) from ProjectFile pf"
            + " where pf.projectVersion=:" + parVersionId
            + " and pf.status=:" + parFileStatus;
        // Execute the query
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(parVersionId,    pv);
        parameters.put(parFileStatus,   state);
        List<?> queryResult = dbs.doHQL(query, parameters);
        // Return the query's result (if found)
        if(queryResult != null || queryResult.size() > 0)
            return (Long) queryResult.get(0);
        // Default result
        return 0;
    }
    
    public String toString() {
        return this.project.getName() + "-" + "r" + this.getVersion();
    }
}

//vi: ai nosi sw=4 ts=4 expandtab

