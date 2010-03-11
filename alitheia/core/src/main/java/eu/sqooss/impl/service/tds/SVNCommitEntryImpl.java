/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 - 2010 - Organization for Free and Open Source Software,  
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

package eu.sqooss.impl.service.tds;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.PathChangeType;
import eu.sqooss.service.tds.CommitCopyEntry;

public class SVNCommitEntryImpl implements CommitEntry {
    private SVNProjectRevision revision;

    private String author;

    private String message;

    private Map<String, PathChangeType> changedPaths;

    private List<CommitCopyEntry> copyOps;
    
    @SuppressWarnings("unchecked")
    public SVNCommitEntryImpl(SVNLogEntry l, String root) {
        revision = new SVNProjectRevision(l.getRevision());
        author = l.getAuthor();
        message = l.getMessage();
        revision.setDate(l.getDate());
        changedPaths = new LinkedHashMap<String, PathChangeType>();
        copyOps = new ArrayList<CommitCopyEntry>();
        
        
        Map<String, SVNLogEntryPath> paths = 
            (Map<String, SVNLogEntryPath>) l.getChangedPaths();
        
        for (Iterator i = paths.keySet().iterator(); i.hasNext();) {
            String path = (String) i.next();
            if (path.startsWith(root)) {
            	changedPaths.put(
            	        path, parseSVNLogEntryPath(
            	                paths.get(path).getType()));
            }
            
            String copyPath = paths.get(path).getCopyPath();
            Long   copyRev = paths.get(path).getCopyRevision();
            
            if ((copyPath != null) && (copyRev != -1)) {
                copyOps.add(new CommitCopyEntry(copyPath, new SVNProjectRevision(copyRev), path, revision));
            }
        }
        
        
    }

    public SVNProjectRevision getRevision() {
        return revision;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return revision.getDate();
    }

    public Set<String> getChangedPaths() {
        return changedPaths.keySet();
    }

    public Map<String, PathChangeType> getChangedPathsStatus() {
        return changedPaths;
    }

    public String toString() {
        return getRevision().toString() + " " + getAuthor() + "\n    "
                + getMessage();
    }

    private PathChangeType parseSVNLogEntryPath(char entryPathType) {
        if (entryPathType == SVNLogEntryPath.TYPE_ADDED) {
            return PathChangeType.ADDED;
        } else if (entryPathType == SVNLogEntryPath.TYPE_DELETED) {
            return PathChangeType.DELETED;
        } else if (entryPathType == SVNLogEntryPath.TYPE_MODIFIED) {
            return PathChangeType.MODIFIED;
        } else if (entryPathType == SVNLogEntryPath.TYPE_REPLACED) {
            return PathChangeType.REPLACED;
        } else {
            return PathChangeType.UNKNOWN;
        }
    }

    public List<CommitCopyEntry> getCopyOperations() {
        return copyOps;
    }
}

// vi: ai nosi sw=4 ts=4 expandtab

