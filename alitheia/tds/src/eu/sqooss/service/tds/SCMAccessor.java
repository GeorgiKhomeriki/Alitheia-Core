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

import java.io.FileNotFoundException;

import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.Diff;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.ProjectRevision;

public interface SCMAccessor {
    /**
     * Get the numeric revision number for HEAD in this project.
     * Returns a negative value (usually -1) on error.
     */
    public long getHeadRevision();

    /**
     * Retrieve a checkout of the complete source tree underneath
     * the given path, relative to the root URL of the project
     * to which this accessor is attached. The checkout is written
     * to the local path @p localPath .
     */
    public void checkOut( String repoPath, ProjectRevision revision, String localPath )
        throws InvalidProjectRevisionException;

    /**
     * Retrieve a single file from the source repository, relative
     * to the root URL of the project to which this accessor is
     * attached. The checked-out file is written to the local
     * path @p localPath.
     */
    public void checkOutFile( String repoPath, ProjectRevision revision, String localPath )
        throws InvalidProjectRevisionException,
               FileNotFoundException;

    /**
     * Get the commit log entries for revisions @p r1 to @p r2
     * for this source repository.
     */
    public CommitLog getCommitLog( ProjectRevision r1, ProjectRevision r2 )
        throws InvalidProjectRevisionException;

    /**
     * Get the commit log entries for revisions @p r1 to @p r2
     * for this source repository within the subtree identified
     * by the path @p repoPath (relative to the root URL of the
     * project this accessor is attached to).
     */
    public CommitLog getCommitLog( String repoPath, ProjectRevision r1, ProjectRevision r2 )
        throws InvalidProjectRevisionException;

    /**
     * Get the diff between two revisions of a subtree within
     * the source repository. Arguments as getCommitLog(), above.
     */
    public Diff getDiff( String repoPath, ProjectRevision r1, ProjectRevision r2 );
}

// vi: ai nosi sw=4 ts=4 expandtab

