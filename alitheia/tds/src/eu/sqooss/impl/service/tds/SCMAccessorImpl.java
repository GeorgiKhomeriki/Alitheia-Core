/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 by Adriaan de Groot <groot@kde.org>
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

package eu.sqooss.impl.service.tds;

import java.util.Collection;
// import java.util.Iterator;
// import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.Diff;
import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.impl.service.tds.CommitLogImpl;

public class SCMAccessorImpl implements SCMAccessor {
    private String url;
    private String projectName;
    private SVNRepository svnRepository = null;
    public static Logger logger = null;

    public SCMAccessorImpl( String projectName, String url ) {
        this.url = url;
        this.projectName = projectName;
        if (logger != null) {
            logger.info("Created SCMAccessor for " + projectName);
        }
    }

    // Interface methods
    public void checkOut( String repoPath, ProjectRevision revision, String localPath ) {
    }

    public void checkOutFile( String repoPath, ProjectRevision revision, String localPath ) {
    }

    private void connectToRepository() {
        try {
            svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));
            // All access is assumed to be anonynmous, so no
            // authentication manager is used.
        } catch (SVNException e) {
            logger.warning("Could not create SVN repository connection for " + projectName +
                e.getMessage());
            svnRepository = null;
        }
    }

    public long getHeadRevision() {
	long endRevision = -1;
        try {
            endRevision = svnRepository.getLatestRevision();
            logger.info("Latest revision of " + projectName + " is " + endRevision);
        } catch (SVNException e) {
            logger.warning("Could not get latest revision of " + projectName +
                e.getMessage());
        }

	return endRevision;
    }

    private long resolveDatedProjectRevision( ProjectRevision r )
        throws SVNException, InvalidProjectRevisionException {
        if ( (r==null) || (!r.hasDate()) ) {
            throw new InvalidProjectRevisionException("Can only resolve a revision with a date");
        }

        long revno = svnRepository.getDatedRevision(r.getDate());
        if (revno > 0) {
            r.setSVNRevision(revno);
        }
        return revno;
    }

    private long resolveProjectRevision( ProjectRevision r )
        throws InvalidProjectRevisionException {
        if ( (r==null) || (!r.isValid()) ) {
            throw new InvalidProjectRevisionException("Can only resolve a valid revision");
        }

        if (r.hasSVNRevision()) {
            return r.getSVNRevision();
        } else {
            try {
                return resolveDatedProjectRevision(r);
            } catch (SVNException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public CommitLog getCommitLog( ProjectRevision r1, ProjectRevision r2 )
        throws InvalidProjectRevisionException {
        return getCommitLog("",r1,r2);
    }

    public CommitLog getCommitLog( String repoPath, ProjectRevision r1, ProjectRevision r2 )
        throws InvalidProjectRevisionException {
        if (svnRepository == null) {
            connectToRepository();
        }
        if (svnRepository == null) {
            throw new NullPointerException("No SVN repository for project <" + projectName + ">");
        }
        if (r1 == null) {
            throw new InvalidProjectRevisionException("Null start revision");
        }
        if (!r1.isValid()) {
            throw new InvalidProjectRevisionException("Invalid start revision");
        }

        // Map the project revisions to SVN revision numbers
        long revstart=-1, revend=-1;
        revstart = resolveProjectRevision(r1);
        logger.info("Start revision for log " + r1);

        if (r2 == null) {
            revend = revstart;
        } else {
            if (!r2.isValid()) {
                throw new InvalidProjectRevisionException("Invalid end revision");
            }
            revend = resolveProjectRevision(r2);
            logger.info("End revision for log " + r2);
        }

        getHeadRevision();

        CommitLogImpl l = new CommitLogImpl();
        try {
            Collection logEntries = svnRepository.log(new String[]{repoPath},
                l.getEntriesReference(),
                revstart, revend, true, true);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }

        return l;
    }

    public Diff getDiff( String repoPath, ProjectRevision r1, ProjectRevision r2 ) {
        logger.info("diff -r" +
            r1.getSVNRevision() + ":" + r2.getSVNRevision() + " " + repoPath);
        return null;
    }
}

// vi: ai nosi sw=4 ts=4 expandtab

