/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Adriaan de Groot <groot@kde.org>
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

package eu.sqooss.impl.service.fds;

import java.io.FileNotFoundException;

import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.fds.InMemoryCheckout;
import eu.sqooss.service.fds.InMemoryDirectory;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.InvalidRepositoryException;
import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.SCMAccessor;

/**
 * The CheckoutImpl implements the Checkout interface. It represents a
 * checkout of a specific project at a specific revision somewhere in the
 * filesystem of the Alitheia core system. A CheckoutImpl exposes
 * additional API for updating the checkout itself and handling the
 * reference counting done on it. Most operations on CheckoutImpl
 * are not thread-safe. Locking is done in the FDS which exposes
 * only the Checkout (safe) part of the interface.
 */
class InMemoryCheckoutImpl implements InMemoryCheckout {
    /**
     * The project this checkout belongs to. Used to get back to the
     * TDS SCM that can update the checkout.
     */
    private long projectId;
    /**
     * Human-readable project name. Informational only.
     */
    private String projectName;

    private String repoPath;
    private ProjectRevision revision;
    private CommitEntry entry;

    private InMemoryDirectory root;
    
    private int claims;
    
    InMemoryCheckoutImpl(SCMAccessor scm, String path, ProjectRevision r)
        throws FileNotFoundException,
               InvalidProjectRevisionException,
               InvalidRepositoryException {
        projectId = scm.getId();
        projectName = scm.getName();
        repoPath = path;
        root = new InMemoryDirectory(this);
        
        scm.getCheckout(path, r, root.createSubDirectory(scm.getSubProjectPath()));
        entry = scm.getCommitLog(repoPath, r);

        setRevision(r);
    }

    public int claim() {
        return ++claims;
    }

    public int release() {
        return --claims;
    }

    public int getReferenceCount() {
        return claims;
    }

    public void updateCheckout(SCMAccessor scm, ProjectRevision r)
        throws FileNotFoundException,
               InvalidProjectRevisionException,
               InvalidRepositoryException {
        scm.updateCheckout(repoPath, getRevision(), r, root);
        entry = scm.getCommitLog(repoPath, r);
        setRevision(r);
    }

    public void setRevision(ProjectRevision r) {
        this.revision = r;
    }

    // Interface methods
    /** {@inheritDoc} */
    public ProjectRevision getRevision() {
        return revision;
    }

    public StoredProject getProject() {
        return StoredProject.getProjectByName(projectName);
    }
    
    public InMemoryDirectory getRoot() {
        return root;
    }
    
    public CommitEntry getCommitLog() {
        return entry;
    }

    // Interface eu.sqooss.service.tds.NamedAccessor
    /** {@inheritDoc} */
    public String getName() {
        return projectName;
    }

    /** {@inheritDoc} */
    public long getId() {
        return projectId;
    }
    
    public ProjectFile getFile(String name) {
    	return root.getFile(name);
    }
}



// vi: ai nosi sw=4 ts=4 expandtab

