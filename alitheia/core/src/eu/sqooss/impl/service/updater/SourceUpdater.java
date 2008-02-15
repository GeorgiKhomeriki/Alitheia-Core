/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 Georgios Gousios <gousiosg@gmail.com>
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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.abstractmetric.Metric;
import eu.sqooss.service.abstractmetric.MetricMismatchException;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.db.Tag;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.InvalidRepositoryException;
import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.PathChangeType;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterException;
import eu.sqooss.service.updater.UpdaterService;

class SourceUpdater extends Job {
    private UpdaterServiceImpl updater;
    private StoredProject project;
    private TDSService tds;
    private DBService dbs;
    private Logger logger;
    private AlitheiaCore core;
    private ServiceReference[] versionMetrics = null;
    private ServiceReference[] fileMetrics = null;

    public SourceUpdater(StoredProject project, UpdaterServiceImpl updater, AlitheiaCore core, Logger logger) throws UpdaterException {
        if ((project == null) || (core == null) || (logger == null)) {
            throw new UpdaterException(
                    "The components required by the updater are unavailable.");
        }

        this.project = project;
        this.updater = updater;
        this.logger = logger;
        this.core = core;
        this.tds = core.getTDSService();
        this.dbs = core.getDBService();
        
        versionMetrics = core.getPluginManager().listMetricProviders(ProjectVersion.class);
        fileMetrics = core.getPluginManager().listMetricProviders(ProjectFile.class);
    }

    public int priority() {
        return 1;
    }

    protected void run() {
        /* 
         * Cache project version and project file IDs for kick-starting
         * metric update jobs after the metadata update. This is done
         * to avoid holding references to huge data graphs on large 
         * updates
         */
        SortedSet<Long> updProjectVersions = new TreeSet();
        SortedSet<Long> updFiles = new TreeSet();

        logger.info("Running source update for project " + project.getName());
        Session s = dbs.getSession(this);
        long ts = System.currentTimeMillis();
        
        try {

            // This is the last version we actually know about
            ProjectVersion lastVersion = StoredProject.getLastProjectVersion(project, logger);
            SCMAccessor scm = tds.getAccessor(project.getId()).getSCMAccessor();
            long lastSCMVersion = scm.getHeadRevision();
            CommitLog commitLog = scm.getCommitLog(
                    new ProjectRevision(lastVersion.getVersion()),
                    new ProjectRevision(lastSCMVersion));

            logger.info(project.getName() + ": Log entries: " + commitLog.size());
            logger.info(project.getName() + ": Time to get log: " + 
                    (int)((System.currentTimeMillis() - ts)/1000));
            ts = System.currentTimeMillis();
            
            for (CommitEntry entry : commitLog) {
                Transaction tx = s.beginTransaction();
                //handle changes that have occurred on each individual commit
                // and create the necessary jobs for storing information
                //related to updated files
                logger.info(entry.toString());

                ProjectVersion curVersion = new ProjectVersion();
                curVersion.setProject(project);
                // Assertion: this value is the same as lastSCMVersion
                curVersion.setVersion(entry.getRevision().getSVNRevision());
                s.save(curVersion);
                updProjectVersions.add(new Long(curVersion.getId()));
                
                for(String chPath: entry.getChangedPaths()) {

                    if(isTag(entry, chPath)) {
                        logger.info(project.getName() + ": SVN Tag revision: " + 
                                entry.getRevision().getSVNRevision());
                        Tag t = curVersion.addTag();
                        t.setName(chPath.substring(5));
                        s.save(t);
                        break;
                    }

                    ProjectFile pf = curVersion.addProjectFile();
                    pf.setName(chPath);
                    pf.setStatus(entry.getChangedPathsStatus().get(chPath).toString());
                    logger.info(project.getName() + ": Saving path: " + chPath);
                    s.save(pf);
                    updFiles.add(pf.getId());
                }
                try {
                    /*Commits a project version along with changes 
                     * in project paths or tags
                     */
                    tx.commit();
                    
                } catch (HibernateException e) {
                    if (tx != null) {
                        try {
                            tx.rollback();
                        } catch (HibernateException ex) {
                            logger.error("Error while rolling back failed transaction"
                                    + ". DB may be left in inconsistent state:"
                                    + ex.getMessage());
                            ex.printStackTrace();
                        }
                        logger.error("Failed to commit updates to the database: "
                                + e.getMessage() + " Transaction rollbacked");
                    }
                    setState(State.Error);
                    break;
                }
            }
        } catch (InvalidRepositoryException e) {
            logger.error("Not such repository:" + e.getMessage());
            setState(State.Error);
        } catch (InvalidProjectRevisionException e) {
            logger.error("Not such repository revision:" + e.getMessage());
            setState(State.Error);
        } finally {

        logger.info(project.getName() + ": Time to process entries: "
                    + (int) ((System.currentTimeMillis() - ts) / 1000));

            /* Start project version metrics */
            Iterator<Long> i = updProjectVersions.iterator();

            while (i.hasNext()) {
                for (ServiceReference r : versionMetrics) {
                    Metric m = (Metric) core.getService(r);
                    if (m != null) {
                        try {
                            m.run(dbs.findObjectById(ProjectVersion.class, i.next().longValue()));
                        } catch (MetricMismatchException e) {
                            logger.warn("Metric " + m.getName() + " failed");
                        }
                    }
                }
            }

            /* Start project file metrics */
            i = updFiles.iterator();

            while (i.hasNext()) {
                for (ServiceReference r : fileMetrics) {
                    Metric m = (Metric) core.getService(r);
                    if (m != null) {
                        try {
                            m.run(dbs.findObjectById(ProjectFile.class, i.next().longValue()));
                        } catch (MetricMismatchException e) {
                            logger.warn("Metric " + m.getName() + " failed");
                        }
                    }
                }
            }

            dbs.returnSession(s);
            updater.removeUpdater(project.getName(), UpdaterService.UpdateTarget.CODE);
        }
    }

    /**
     * Tell tags from regular commits (heuristic based)
     *
     * @param entry
     * @param path
     * @return True if <tt>entry</tt> represents a tag
     */
    private boolean isTag(CommitEntry entry, String path) {
        if(!path.startsWith("/tags"))
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
}

// vi: ai nosi sw=4 ts=4 expandtab
