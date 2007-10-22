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

package eu.sqooss.service.tds;

import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.InvalidProjectRevisionException;

public interface CommitLog {
    /**
     * Retrieve the project revision information for the first
     * entry in this commit log. May return null if the log is empty.
     */
    public ProjectRevision first();

    /**
     * Retrieve the project revision information for the last
     * entry in this commit log. This may be the same as first()
     * for 1-entry logs. May return null if the log is empty.
     */
    public ProjectRevision last();

    /**
     * Retrieve the message (commit message) for project revision
     * @p r in this log. If @p r is not valid in some way, throw
     * an exception. If @p r is not in the log (the revision does not
     * occur, for instance) return null. For ProjectRevisions with
     * no SVN revision attached (date revisions) return the last
     * revision that is not after the indicated date, or null if there
     * isn't one.
     */
    public String message(ProjectRevision r)
        throws InvalidProjectRevisionException;

    /**
     * For debugging purposes, dump the log to stdout.
     */
    public void dump();
}

// vi: ai nosi sw=4 ts=4 expandtab

