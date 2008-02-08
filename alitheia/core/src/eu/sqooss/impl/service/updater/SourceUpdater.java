/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 * 
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 Georgios Gousios <gousiosg@aueb.gr>
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.Diff;
import eu.sqooss.service.tds.PathChangeType;
import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterException;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.scheduler.Scheduler;

/**
 * 
 * @author Kostas Stroggylos
 */
public class SourceUpdater extends Job {

    private AlitheiaCore core;
    private BundleContext context;
    private String path;
    private TDSService tds;
    private DBService dbs;
    private Logger logger;
    private List<Job> subTasks;

    public SourceUpdater(String path, AlitheiaCore core, Logger logger, BundleContext bc) throws UpdaterException {
        if ((path == null) || (core == null) || (logger == null) || (bc == null)) {
            throw new UpdaterException(
                    "The components required by the updater are unavailable.");
        }

        this.core = core;
        this.context = bc;
        this.logger = logger;
        this.tds = core.getTDSService();
        this.dbs = core.getDBService();
        subTasks = new ArrayList<Job>();
    }
    
    public int priority() {
        return 1;
    }

    protected void run() throws UpdaterException {
        try {
            StoredProject project = StoredProject.getProject(path, dbs, logger);
            if(project == null) {
                //the project was not found, so the job can not continue
                logger.error("The project with the given name was not found");
                setState(State.Error);
                return;
            }
            ProjectVersion lastVersion = StoredProject.getLastProjectVersion(project, dbs, logger);
            SCMAccessor scm = tds.getAccessor(project.getId()).getSCMAccessor();
            CommitLog commitLog = scm.getCommitLog(new ProjectRevision(
                    lastVersion.getVersion()), new ProjectRevision(new Date()));

            ProjectVersion curVersion = new ProjectVersion();
            curVersion.setProject(project);
            curVersion.setVersion((int)commitLog.last().getSVNRevision());
            //TODO: switch ProjectVersion.version to long
            dbs.addRecord(curVersion);
            
            for (CommitEntry entry : commitLog) {
                //handle changes that have occured on each individual commit 
                // and create the necessary jobs for storing information 
                //related to updated files
                processEntry(entry, curVersion);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            setState(State.Error);
        }
    }
    
    private Diff getProjectDiff(ProjectVersion lastVersion, SCMAccessor scm)
            throws UpdaterException {
        Diff diff = null;
        try {
            diff = scm.getDiff("/", new ProjectRevision(lastVersion
                    .getVersion()), new ProjectRevision(new Date()));
        } catch (Exception ex) {
            throw new UpdaterException(
                    "Updater failed to retrieve the diff for the project "
                            + path + ":\n" + ex.getMessage());
        }
        return diff;
    }
    
    private void processEntry(CommitEntry entry, ProjectVersion pv) throws Exception {
        Map<String, PathChangeType> changes = entry
        .getChangedPathsStatus();

        for (Iterator i = changes.keySet().iterator(); i.hasNext();) {
            String path = (String) i.next();
            PathChangeType change = changes.get(path);
            
            CommitEntryHandlerJob job = new CommitEntryHandlerJob(core, logger);
            job.init(pv, path, change);
            this.addDependency(job);
            subTasks.add(job);
        }
        
    }
    
    @Override
    protected void aboutToBeEnqueued(Scheduler s) {
        try {
            for(Job subTask: subTasks) {
                s.enqueue(subTask);
            }
        }
        catch(Exception e) {
            logger.error("Updater failed to enqueue jobs for handling updated source files");
            return;
        }
    }
    
    @Override
    protected void aboutToBeDequeued(Scheduler s) {
        //remove the subTasks from the queue
        for(Job subTask: subTasks) {
            try {
                s.enqueue(subTask);
            }
            catch(Exception e) {
                logger.error("Updater failed to dequeue jobs for handling updated source files");
                continue;
            }
        }
        super.aboutToBeDequeued(s);
    }
}

// vi: ai nosi sw=4 ts=4 expandtab
