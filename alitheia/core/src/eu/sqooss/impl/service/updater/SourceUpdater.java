/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
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

package eu.sqooss.impl.service.updater;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.LRUMap;
import org.hibernate.QueryException;
import org.hibernate.exception.SQLGrammarException;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Developer;
import eu.sqooss.service.db.Directory;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectFileState;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.db.Tag;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.tds.CommitCopyEntry;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.InvalidRepositoryException;
import eu.sqooss.service.tds.PathChangeType;
import eu.sqooss.service.tds.Revision;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.SCMNode;
import eu.sqooss.service.tds.SCMNodeType;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterException;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.util.FileUtils;

final class SourceUpdater extends Job {
    
    private static final String HANDLE_COPIES_PROPERTY = "eu.sqooss.updater.svn.handlecopies";
    
    private enum HandleCopies {
        TRUNK, BRANCHES, TAGS
    }
    
    private UpdaterServiceImpl updater;
    private StoredProject project;
    private TDSService tds;
    private DBService dbs;
    private Logger logger;
    private MetricActivator ma;
    
    /* Flag set on start up */
    private HandleCopies hc = HandleCopies.BRANCHES;
    private boolean supportStoredProcedureUpdates; 
    
    /*
     * Hold project version and project file IDs for kick-starting
     * metric update jobs after the metadata update. This is done
     * to avoid holding references to huge data graphs during large
     * updates.
     */
    private Set<Long> updProjectVersions = new TreeSet<Long>();
    private Set<Long> updFiles = new TreeSet<Long>();
    private Set<Long> updDevs = new TreeSet<Long>();
    
    /* Avoid Hibernate thrashing by caching frequently accessed directories */
    private LRUMap dirCache = new LRUMap(200);
    
    /* Container for all project file records for each processed version*/
    private List<ProjectFile> versionFiles = new ArrayList<ProjectFile>();
    
    /* Currently processed commit log entry*/
    private CommitEntry commitLogEntry;
    
    /* 
     * State weights to use when evaluating duplicate project file entries
     * in a single revision
     */
    private static Map<Integer, Integer> stateWeights ;
    
    /*SCM accessor to the project's repository */
    private SCMAccessor scm = null;
    
    static {
        stateWeights = new HashMap<Integer, Integer>();
        stateWeights.put(ProjectFileState.STATE_ADDED, 2);
        stateWeights.put(ProjectFileState.STATE_MODIFIED, 4);
        stateWeights.put(ProjectFileState.STATE_REPLACED, 8);
        stateWeights.put(ProjectFileState.STATE_DELETED, 16);
    }
    
    /*
     * A Map from SCM file status codes represented as strings
     * to system internal ids.
     */
    private static Map<String, Integer> SCMStatusIDs;
    static {
        SCMStatusIDs = new HashMap<String, Integer>();
        SCMStatusIDs.put("ADDED", ProjectFileState.STATE_ADDED);
        SCMStatusIDs.put("MODIFIED", ProjectFileState.STATE_MODIFIED);
        SCMStatusIDs.put("DELETED", ProjectFileState.STATE_DELETED);
        SCMStatusIDs.put("REPLACED", ProjectFileState.STATE_REPLACED);
    }
    
    
    /**
     * Update SVN metadata by synchronizing the latest version available to
     * the metadata database with the latest version available to the 
     * repository.
     *  
     * @param project The project to perform an update on
     * @param updater The updater instance that runs the update
     * @param core A reference to the core service
     * @param logger A preconfigured logger, common to all updaters
     * @throws UpdaterException When things go wrong
     */
    public SourceUpdater(StoredProject project, UpdaterServiceImpl updater,
            AlitheiaCore core, Logger logger) throws UpdaterException {
        if ((project == null) || (core == null) || (logger == null)) {
            throw new UpdaterException(
                    "The components required by the updater are unavailable.");
        }

        this.project = project;
        this.updater = updater;
        this.logger = logger;
        this.tds = core.getTDSService();
        this.dbs = core.getDBService();
        this.ma = core.getMetricActivator();
        
        String hcp = System.getProperty(HANDLE_COPIES_PROPERTY);
        
        if (hcp != null) {
            if (hcp.equalsIgnoreCase("trunk")) {
                hc = HandleCopies.TRUNK;
            } else if (hcp.equalsIgnoreCase("branches")) {
                hc = HandleCopies.BRANCHES;
            } else if (hcp.equalsIgnoreCase("tags")) {
                hc = HandleCopies.TAGS;
            } else {
                warn("Not correct value for property " + HANDLE_COPIES_PROPERTY);
            }
        } else {
            info("No value for " + HANDLE_COPIES_PROPERTY + " property," +
            		" using default:" + this.hc);
        }
    }

    public int priority() {
        return 0x1;
    }

    /**
     * @see eu.sqooss.service.scheduler.Job#run()
     *
     * @throws Exception as per the general contract of Job.run()
     */
    protected void run() throws Exception {
        
        dbs.startDBSession();
        
        setSupportsStoredProcedureUpdates();
        
        int numRevisions = 0;
        
        info("Running source update for project " + project.getName() 
                + " ID " + project.getId());
        
        CommitLog commitLog = null;
        
        try {
            // This is the last version we actually know about
            ProjectVersion latestVersion = ProjectVersion.getLastProjectVersion(project);
            scm = tds.getAccessor(project.getId()).getSCMAccessor();
            if (latestVersion != null) {  
                Revision r = scm.getHeadRevision();
                
                /* Don't choke when called to update an up-to-date project */
                if (r.compareTo(scm.newRevision(latestVersion.getRevisionId())) <= 0) {
                    info("Project is already at the newest version " 
                            + r.getUniqueId());
                    dbs.commitDBSession();
                    return;    
                }
            } else {
                //Add revision 0 and / (root) file entry
                ProjectVersion zero = new ProjectVersion(project);
                zero.setCommitter(Developer.getDeveloperByUsername("sqo-oss", project));
                zero.setTimestamp(scm.getFirstRevision().getDate().getTime());
                zero.setCommitMsg("Artificial revision to include / directory");
                zero.setRevisionId("0");
                zero.setSequence(0);
                dbs.addRecord(zero);
                ProjectFile root = new ProjectFile(zero);
                root.setIsDirectory(true);
                root.setDir(getDirectory("/", true));
                root.setName("");
                root.setState(ProjectFileState.added());
                root.setValidFrom(zero);
                root.setValidUntil(zero);
                dbs.addRecord(root);
                HashSet<ProjectFile> ffv = new HashSet<ProjectFile>();
                ffv.add(root);
                latestVersion = ProjectVersion.getLastProjectVersion(project);
            }
            commitLog = scm.getCommitLog(scm.getNextRevision(
                    scm.newRevision(latestVersion.getRevisionId())), 
                    scm.getHeadRevision());
            info("New revisions: " + commitLog.size());
            
            for (CommitEntry entry : commitLog) {
                versionFiles.clear();
                commitLogEntry = entry;
                ProjectVersion curVersion = processCommit(scm, entry);

                /*
                 * Process copy operations prior to normal operations. After a
                 * copy, a lot of things can happen, for example deleting or
                 * adding files in copied path. Placing copy processing before
                 * normal operation processing ensures that all the files are in
                 * place before all operations that modify the copied paths
                 * start being processed. This actually resembles the way a
                 * local checkout works: the user first copies a path, then
                 * modifies the files in the copied path. For non copied paths,
                 * this has no effect in any case.
                 */
                processCopyOps(scm, entry, curVersion, getPreviousVersion(scm,
                        curVersion));

                /*
                 * Now process normal operations.
                 */
                processNormalOps(scm, entry, curVersion);

                /*
                 * For each directory whose contents were modified, add a
                 * modified entry.
                 */
                addModifiedDirEntries(curVersion);

                /*
                 * Replay the SVN on the intermediate files to remove
                 * duplicates. Handles cases such as when a file was copied and
                 * modified in the same revision.
                 */
                replayLog(curVersion);
                dbs.addRecords(versionFiles);

                /*
                 * Update the list of live files in this revision. 
                 */
                if (supportStoredProcedureUpdates)
                	updateValidUntilProc(curVersion);
                else
                	updateValidUntil(curVersion);

                numRevisions++;
                dirCache.clear();

                if (!dbs.commitDBSession()) {
                    warn("Intermediate commit failed, restarting update");
                    restart();
                    return;
                }
                dbs.startDBSession();
            }
            info("Processed " + numRevisions + " revisions");
        } catch (InvalidRepositoryException e) {
            err("Not such repository:" + e.getMessage());
            throw e;
        } catch (InvalidProjectRevisionException e) {
            err("Not such repository revision:" + e.getMessage());
            throw e;
        } finally {
            dbs.commitDBSession();
            //Run the metrics even if the update fails, to ensure that 
            //the versions that were processed correctly will be measured
            updater.removeUpdater(project.getName(), UpdaterService.UpdateTarget.CODE);
            ma.runMetrics(updFiles, ProjectFile.class);
            ma.runMetrics(updProjectVersions, ProjectVersion.class);
            ma.runMetrics(updDevs, Developer.class);
        }
    }
   
    private void setSupportsStoredProcedureUpdates() {
    	String paramVersion = "paramVersion";
 	    String paramPrevVersion = "paramPrev";
     	String paramState = "paramStatus";
     	
    	List<String> arglist = new ArrayList<String>();
    	arglist.add(paramPrevVersion);
    	arglist.add(paramVersion);
    	arglist.add(paramState);
     	
    	Map<String,Object> params = new HashMap<String,Object>();
     	params.put(paramState, 0);
     	params.put(paramVersion, 0);
     	params.put(paramPrevVersion, 0);
     	
     	try {
			dbs.callProcedure("updatelivefiles", arglist, params);
			this.supportStoredProcedureUpdates = true;
		} catch (Exception e) {
			this.supportStoredProcedureUpdates = false;
		} finally {
			if (!dbs.isDBSessionActive())
				dbs.startDBSession();
		}
	}

	/*
     * This method processes project version metadata.
     */
    private ProjectVersion processCommit(SCMAccessor scm, CommitEntry entry) {
        ProjectVersion curVersion = new ProjectVersion(project);
        // Assertion: this value is the same as lastSCMVersion
        curVersion.setRevisionId(entry.getRevision().getUniqueId());
        curVersion.setTimestamp(entry.getDate().getTime());

        Developer d  = Developer.getDeveloperByUsername(entry.getAuthor(), project);
        if (!updDevs.contains(d.getId())) {
            updDevs.add(d.getId());
        }
        curVersion.setCommitter(d);

        /* TODO: get column length info from Hibernate */
        String commitMsg = entry.getMessage();
        if (commitMsg.length() > 512) {
            commitMsg = commitMsg.substring(0, 511);
        }

        curVersion.setCommitMsg(commitMsg);
        ProjectVersion prev = getPreviousVersion(scm, curVersion);
        curVersion.setSequence(prev.getSequence() + 1);
        dbs.addRecord(curVersion);

        debug("Got version " + curVersion.getRevisionId() + 
                " ID " + curVersion.getId());
        return curVersion;
    }

    /*
     * Copy operations copy or move files or directories accross
     * the virtual filetree generated by the SCM.
     */
    private void processCopyOps(SCMAccessor scm, CommitEntry entry,
            ProjectVersion curVersion, ProjectVersion prev)
            throws InvalidProjectRevisionException, InvalidRepositoryException {
        for (CommitCopyEntry cce : entry.getCopyOperations()) {
            
        	if (!canProcess(cce.fromPath(), cce.toPath())) {
        		return; 
        	}
        	
            ProjectFile copyFrom = null;
            copyFrom = ProjectFile.findFile(project.getId(), 
                        FileUtils.basename(cce.fromPath()), 
                        FileUtils.dirname(cce.fromPath()), 
                        cce.fromRev().getUniqueId());
                
            /* Source location is an entry we do not have info for, 
             * due to updater settings. Use the SCM to retrieve
             * the missing info.
             */
            if (copyFrom == null) {
                warn("expecting 1 got " + 0 + " files for path " 
                        + cce.fromPath() + " " + prev.toString());
                SCMNodeType type = scm.getNodeType(cce.fromPath(), cce.fromRev());
                
                if (type.equals(SCMNodeType.FILE)) {
                	addFile(curVersion, cce.toPath(), ProjectFileState.added(), 
                			SCMNodeType.FILE, copyFrom);
                } else if (type.equals(SCMNodeType.DIR)) {
                	
                	SCMNode n = scm.getNode(cce.fromPath(), cce.fromRev());
                	
                	if (n != null) {
                		debug("Copying directory");
                		handleDirCopyFromRepository(curVersion, n, cce.toPath());
                	} else {
                		warn("Directory " + cce.fromPath() + " cannot be found");
                	}
                } else {
                	warn("Path " + cce.fromPath() + " is of uknown type " 
                			+ type + " which the updater cannot process");
                }
                
                continue;
            }
                
            if (copyFrom.getIsDirectory()) {
                    
                Directory from = getDirectory(cce.fromPath(), false);
                Directory to = getDirectory(cce.toPath(), true);

                /*
                 * Recursively copy contents and mark files as modified
                 * and directories as added
                 */
                debug("Copying directory " + from.getPath()
                        + " (from r" + cce.fromRev().getUniqueId()
                        + ") to " + to.getPath());
                handleDirCopy(curVersion, 
                        ProjectVersion.getVersionByRevision(curVersion.getProject(),
                        cce.fromRev().getUniqueId()), from, to, copyFrom);
            } else {
                /*
                 * Create a new entry at the new location and mark the new 
                 * entry as ADDED
                 */
                addFile(curVersion, cce.toPath(), ProjectFileState.added(), 
                		SCMNodeType.FILE, copyFrom);
            }   
        }
    }

	/*
     * Normal operations are the operations that do not copy files or are
     * not a result of a file copy.
     */
    private void processNormalOps(SCMAccessor scm, CommitEntry entry,
            ProjectVersion curVersion) throws InvalidRepositoryException {
        for (String chPath : entry.getChangedPaths()) {
            
            SCMNodeType t = scm.getNodeType(chPath, entry.getRevision());
            /*
             * We make the assumption that tags entries can only be
             * directories, based on info obtained from the SVN manual See:
             * http://svnbook.red-bean.com/en/1.1/ch04s06.html
             */
            if (t == SCMNodeType.DIR && isTag(entry, chPath)) {

                Tag tag = new Tag(curVersion);
                tag.setName(chPath.split("tags/")[1]);
                debug("Creating tag <" + tag.getName() + ">");

                dbs.addRecord(tag);
                break;
            }
            
            if(!canProcess(chPath, null))
                continue; //Entries under tags or branches
            
            //Dirs are processed recursively. This allows
            //copy-and-modify-in-copied-dir scenarions to be processed
            if (isCopiedPath(chPath) && t != SCMNodeType.FILE) 
                continue; //Processed earlier

            ProjectFile toAdd = null;
            
            toAdd = addFile(curVersion, chPath,
                    ProjectFileState.fromStatus(SCMStatusIDs.get(
                    entry.getChangedPathsStatus().get(chPath).toString())), 
                    t, null);
            
            /*
             * Before entering the next block, examine whether the deleted
             * file was a directory or not. If there is no path entry in the
             * Directory table for the processed file path, this means that
             * the path is definitely not a directory. If there is such an
             * entry, it may be shared with another project; this case is
             * examined upon entering
             */
            if (toAdd.isDeleted() && (getDirectory(chPath, false) != null)) {
                /*
                 * Directories, when they are deleted, do not have type DIR,
                 * but something else. So we need to check on deletes
                 * whether this name was most recently a directory.
                 */
                ProjectFile lastIncarnation = ProjectFile.getPreviousFileVersion(toAdd);
                
                /*
                 * If a directory is deleted and its previous incarnation cannot
                 * be found in a previous revision, this means that the
                 * directory is deleted in the same revision it was added
                 * (probably copied first)! Search in the current
                 * revision files then.
                 */
                boolean delAfterCopy = false;
                if (lastIncarnation == null) {
                    for (ProjectFile pf : versionFiles) {
                        if (pf.getFileName().equals(toAdd.getFileName())
                                && pf.getIsDirectory()
                                && pf.isAdded()) {
                            lastIncarnation = pf;
                            delAfterCopy = true;
                            break;
                        }
                    }
                }
                    
                /* If a dir was deleted, mark all children as deleted */
                if (lastIncarnation != null
                        && lastIncarnation.getIsDirectory()) {
                    // In spite of it not being marked as a directory
                    // in the node tree right now.
                    toAdd.setIsDirectory(true);
                } else if (!delAfterCopy) {
                    warn("Cannot find previous version of DELETED" +
                                " directory " + toAdd.getFileName());
                }
                
                if (!delAfterCopy) {
                    handleDirDeletion(toAdd, curVersion);
                } else {
                    handleCopiedDirDeletion(toAdd);
                }
            }
        }
    }
    
    /**
     * SVN supports doing weird things on a single file in a single revision.
     * For example, you can copy a file and then delete it or delete it and
     * then copy a new version of the file on it.
     * This method replays the SVN log by merging together various file 
     * modifications recorded in the course of a revision.
     */
    private void replayLog(ProjectVersion curVersion) {
        
        /*Find duplicate projectfile entries*/
        HashMap<String, Integer> numOccurs = new HashMap<String, Integer>();
        for (ProjectFile pf : versionFiles) {
            if (numOccurs.get(pf.getFileName()) != null) {
                numOccurs.put(pf.getFileName(), numOccurs.get(pf.getFileName()).intValue() + 1);
            } else {
                numOccurs.put(pf.getFileName(), 1);
            }
        }
        
        /* Copy list of files to be added to the DB in a tmp array,
         * to use for iterating
         */
        List<ProjectFile> tmpFiles = new ArrayList<ProjectFile>();
        tmpFiles.addAll(versionFiles);
        
        for (String fpath : numOccurs.keySet()) {
            if (numOccurs.get(fpath) <= 1) { 
                continue;
            }
            debug("Multiple entries in single version for file " + fpath);
            
            int points = 0;
            
            ProjectFile copyFrom = null;
            ProjectFile winner = null; 
            
            for (ProjectFile f: tmpFiles) {
                
                if (!f.getFileName().equals(fpath)) { 
                    continue;
                }
                
                debug("  " + f);
                
                if (stateWeights.get(f.getState().getStatus()) > points) {
                    points = stateWeights.get(f.getState().getStatus());
                    if (winner != null)
                        versionFiles.remove(winner);
                    winner = f;
                } else {
                    versionFiles.remove(f);
                }
                
                if (f.getCopyFrom() != null) {
                    copyFrom = f.getCopyFrom();
                }
            }
            
            /*Update file to be added to the DB with copy-from info*/
            if (copyFrom != null) {
                versionFiles.remove(winner);
                winner.setCopyFrom(copyFrom);
                versionFiles.add(winner);
            }
            debug("Keeping file " + winner);
        }
    }
    
    /**
     * On a filesystem, when a file is modified (or added or deleted) in
     * a directory then the directory access time is changed to reflect
     * the change in the contents. To achieve exactly the same effect and also
     * to allow results to be stored against directories, we add a MODIFIED 
     * dir entry for each directory whose contents are changed in a revision. 
     */
    private void addModifiedDirEntries(ProjectVersion pv) {
        //Copy list of version files to an immutable object
        List<ProjectFile> chFiles = new ArrayList<ProjectFile>(this.versionFiles);
        
        for (ProjectFile pf : chFiles) {
            ProjectFile parent = pf.getEnclosingDirectory();
            
            //Parent dir not in the DB, it should be added in this revision
            if (parent == null) {
              continue;  
            }
            
            //Check if parent dir exists in this revision's entries
            boolean exists = false;
            for (ProjectFile dir : chFiles) {
                //Only search directories
                if (!dir.getIsDirectory())
                    continue;
                if (parent.getFileName().equals(dir.getFileName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                //Create it!
                addFile(pv, parent.getFileName(), ProjectFileState.modified(), 
                        SCMNodeType.DIR, null);
            }
        }
    }
    
    /**
     * Constructs a project file out of the provided elements and adds it
     * to the project file cache.
     */
    private ProjectFile addFile(ProjectVersion version, String fPath, 
            ProjectFileState status, SCMNodeType t, ProjectFile copyFrom) {
        ProjectFile pf = new ProjectFile(version);

        String path = FileUtils.dirname(fPath);
        String fname = FileUtils.basename(fPath);

        Directory dir = getDirectory(path, true);
        pf.setName(fname);
        pf.setDir(dir);
        pf.setState(status);
        pf.setCopyFrom(copyFrom);
        pf.setValidFrom(version);
        pf.setValidUntil(version);
        
        if (t == SCMNodeType.DIR) {
            pf.setIsDirectory(true);
            getDirectory(pf.getFileName(), true);
        } else {
            pf.setIsDirectory(false);
        }
        versionFiles.add(pf); 
        debug("Adding file " + pf);
        return pf;
    }
    
    /**
     * Check whether a path is in the list of copy operations for this revision
     */
    private boolean isCopiedPath(String path) {
        boolean copied = false;
        for (CommitCopyEntry copyOp : commitLogEntry.getCopyOperations()) {
            if (path.equals(copyOp.toPath())) {
                copied = true;
                break;
            }
        }

        return copied;
    }
    
    /**
     * Mark the contents of a directory as DELETED when the directory has been
     * DELETED
     * 
     * @param pf The project file representing the deleted directory
     */
    private void handleDirDeletion(ProjectFile pf, ProjectVersion pv) {
        
        if (pf==null || pv==null) {
            throw new IllegalArgumentException("ProjectFile or Version is" +
            		" null in markDeleted()");
        }
        
        if (pf.getIsDirectory() == false) {
            return;
        }
        
        debug("Deleting directory " + pf.getFileName() + " ID "
                + pf.getId());
        Directory d = getDirectory(pf.getFileName(), false);
        if (d == null) {
            warn("Directory entry " + pf.getFileName() + " in project "
                    + pf.getProjectVersion().getProject().getName()
                    + " is missing in Directory table.");
            return;
        }

        ProjectVersion prev = pv.getPreviousVersion();
        
        List<ProjectFile> files = prev.getFiles(d);
        
        for (ProjectFile f : files) {
            if (f.getIsDirectory()) {
                handleDirDeletion(f, pv);
            }
            ProjectFile mark = new ProjectFile(f, pv);
            mark.setState(ProjectFileState.deleted());
            versionFiles.add(mark);
        }
    }
    
    private void handleCopiedDirDeletion(ProjectFile pf) {
        if (pf.getIsDirectory() == false) {
            warn("handleCopiedDirDeletion: path " + pf.getFileName() +
                    " is not a directory");
            return;
        }
        
        List<ProjectFile> files = getVersionFilesInDir(pf);
        
        for (ProjectFile f : files) {
            if (f.getIsDirectory()) {
                handleCopiedDirDeletion(f);
            }
            ProjectFile mark = new ProjectFile(f, f.getProjectVersion());
            mark.setState(ProjectFileState.deleted());
            versionFiles.add(mark);
        }
    }
    
    private List<ProjectFile> getVersionFilesInDir(ProjectFile f) {
        
        if (!f.getIsDirectory()) {
            warn("getVersionFilesInDir: path " + f.getFileName() +
                    " is not a directory");
            return Collections.emptyList();
        }
        
        List<ProjectFile> pfl = new ArrayList<ProjectFile>();
        
        for (ProjectFile pf : versionFiles) {
            if (pf.getDir().getPath().equals(f.getFileName())) {
                pfl.add(pf);
            }
        }
        
        return pfl;
    }
    
    /**
     * Handle directory copies
     */
    private void handleDirCopy(ProjectVersion pv, ProjectVersion fromVersion,
            Directory from, Directory to, ProjectFile copyFrom) {
        
        if (!canProcess(from.getPath(), to.getPath())) 
            return;
       
        addFile(pv, to.getPath(), ProjectFileState.added(), SCMNodeType.DIR, copyFrom);
        
        /*Recursively copy directories*/
        List<ProjectFile> fromPF = fromVersion.getFiles(from, ProjectVersion.MASK_DIRECTORIES);
        
        for (ProjectFile f : fromPF) {
            handleDirCopy(pv, fromVersion, getDirectory(f.getFileName(), false), 
                    getDirectory(to.getPath() + "/" + f.getName(), true), f);
        }
        
        fromPF = fromVersion.getFiles(from, ProjectVersion.MASK_FILES);
        
        for (ProjectFile f : fromPF) {
            addFile(pv, to.getPath() + "/" + f.getName(),
                    ProjectFileState.added(), SCMNodeType.FILE, f);
        }
    }
    
    /**
     * When the option to exclude the processing of certain paths has been 
     * enabled, some directories cannot be found where they should be.
     * In that case, search them in the repository. 
     */
    private void handleDirCopyFromRepository(ProjectVersion pv,
			SCMNode fromFile, String to) {

    	ProjectFile dest = ProjectFile.findFile(project.getId(), 
                FileUtils.basename(to), 
                FileUtils.dirname(to), 
                fromFile.getRevision().getUniqueId());
    	
    	ProjectFileState pfs = ProjectFileState.added();
    	
    	if (dest != null) {
    		pfs = ProjectFileState.modified();
    	}
    	
    	addFile(pv, to, pfs, SCMNodeType.DIR, null);
        
        /*Recursively copy directories*/
        List<SCMNode> dirFiles;
		try {
			dirFiles = scm.listDirectory(fromFile);
		} catch (InvalidRepositoryException e) {
			warn("Cannot list files of SVN directory " + fromFile + 
					" in revision " + fromFile.getRevision());
			return;
		}
        
        for (SCMNode f : dirFiles) {
        	if (f.getType().equals(SCMNodeType.DIR)) {
        		handleDirCopyFromRepository(pv, f, f.getPath());
        	} else {
        		addFile(pv, to + "/" + FileUtils.basename(f.getPath()),
        				ProjectFileState.added(), SCMNodeType.FILE, null);		
        	}
        }
	}
  
    /**
     * Update the validUntil field after all files have been processed. This
     * version uses a stored procedure
     */
    @SuppressWarnings("deprecation")
    private void updateValidUntilProc(ProjectVersion pv) {
    	String paramVersion = "paramVersion";
 	    String paramPrevVersion = "paramPrev";
     	String paramState = "paramStatus";
     	
    	List<String> arglist = new ArrayList<String>();
    	arglist.add(paramPrevVersion);
    	arglist.add(paramVersion);
    	arglist.add(paramState);
     	
    	Map<String,Object> params = new HashMap<String,Object>();
     	params.put(paramState, ProjectFileState.deleted().getId());
     	params.put(paramVersion, pv.getId());
     	params.put(paramPrevVersion, pv.getPreviousVersion().getId());
     	
     	try {
			dbs.callProcedure("updatelivefiles", arglist, params);
		} catch (QueryException e) {
			logger.error("Error calling");
		} catch (SQLException e) {
			logger.error("Error in stored procedure 'updatelivefiles'");
			e.printStackTrace();
		}
    }
    
    /**
     * Update the validUntil field after all files have been processed.
     */
	private void updateValidUntil(ProjectVersion pv) {

    	//Create a lookup table to speedup searching in the processing loop 
    	Map<String, ProjectFile> cache = new HashMap<String, ProjectFile>();
    	
    	for (ProjectFile pf : versionFiles) {
    		cache.put(pf.getFileName(), pf);
    	}
    	
        List<ProjectFile> pfs = pv.getPreviousVersion().getFiles();
        
        for (ProjectFile pf : pfs) {
        	//File was not modified in this revision, bump up the
        	if (cache.get(pf.getFileName()) == null) {
        		pf.setValidUntil(pv);
        		continue;
        	}
        }     
    }
    
	/**
     * Decide whether a path can be copied depending on the value of 
     * the eu.sqooss.updater.handlecopies property.
     */
    private boolean canProcess(String from, String to) {
        
        if (from == null)
            return false;
        
        if (hc.equals(HandleCopies.TAGS)) {
            return true;
        }
        
        if (hc.equals(HandleCopies.TRUNK) && 
               from.contains("trunk") && (
                   (to != null && to.contains("trunk")) || 
                    to == null) 
           ) {
                return true;
        }
        
        if (hc.equals(HandleCopies.BRANCHES)) {
            if ((to != null && to.contains("tags")) || 
                    from.contains("tags")) {
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Uses the SCM to get the previous version to avoid timestamp 
     * inconsistencies 
     */
    private ProjectVersion getPreviousVersion(SCMAccessor scm, ProjectVersion pv) {
        
        Revision p;
        try {
            Revision r = scm.newRevision(pv.getRevisionId());
            p = scm.getPreviousRevision(r);
        } catch (InvalidProjectRevisionException e) {
            err("Cannot get previous version from repository:" 
                    + e.getMessage());
            p = null;
        }
        ProjectVersion prev = null;
        
        if (p != null) {
            prev = ProjectVersion.getVersionByRevision(pv.getProject(), p.getUniqueId());
           
        } 
        
        if (prev == null) {
            warn("Could not get previous revision for project " +
                    "version " + pv);
        }
        
        return prev;
    }
    
    /**
     * Wrapper around 
     * {@link eu.sqooss.service.db.Directory#getDirectory(String, boolean)} 
     * that uses the local object cache before hitting the DB. 
     */
    private Directory getDirectory(String path, boolean create) {
       
        Directory dir = null;
        dir = (Directory) dirCache.get(path);
        if (dir == null) {
            dir = Directory.getDirectory(path, create);
            if (dir != null) {
                dirCache.put(path, dir);
            }
        } 
        return dir;
    }
    
    /**
     * Tell tags from regular commits (heuristic based)
     *  
     * @param entry
     * @param path
     * @return True if <tt>entry</tt> represents a tag
     */
    private boolean isTag(CommitEntry entry, String path) {
        if(!path.contains("/tags/"))
            return false;

        /* Prevent commits that create the tags/ directory
         * from being classified as tags
         */
        if(path.length() <= 5)
            return false;

        /* Tags can only be added (for the time being at least)*/
        if(entry.getChangedPathsStatus().get(path) != PathChangeType.ADDED)
            return false;

        /* If a path is not the prefix for all changed files
         * in a commit, then it is a leaf node (and therefore
         * not a tag)
         */
        for(String chPath: entry.getChangedPaths())
            if(!chPath.startsWith(path))
                return false;

        return true;
    }
    
    private void warn(String message) {
        logger.warn(project.getName() + ":" + message);
    }
    
    private void err(String message) {
        logger.error(project.getName() + ":" + message);
    }
    
    private void info(String message) {
        logger.info(project.getName() + ":" + message);
    }
    
    private void debug(String message) {
        logger.debug(project.getName() + ":" + message);
    }
    
    @Override
    public String toString() {
        return "SourceUpdaterJob - Project:{" + project +"}";
    }
}

// vi: ai nosi sw=4 ts=4 expandtab
