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

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents a lowest-common-denominator interface
 * to diffs obtained between two revisions in a subversion repository
 * (or between two project revisions, whatever that may mean).
 */
public interface Diff {
    
    /**
     * Retrieve the project revision information for the first
     * (before) revision of this diff.
     *
     * @return source revision
     */
    ProjectRevision getSourceRevision();

    /**
     * Retrieve the project revision information for the last
     * revision for this diff. This may be the same as first()
     * for 1-entry diffs (although the difference between R and R
     * is empty).
     *
     * @return comparison revision
     */
    ProjectRevision getTargetRevision();

    /**
     * The diff is stored in a temporary file somewhere. Get
     * the file for it so that the diff itself can be read in.
     *
     * @return abstract file name containing the diff
     */
    File getDiffFile();

    /**
     * Retrieve the list of file names (relative to the root
     * under which this diff was taken) modified by this diff.
     *
     * @return set of files changed in this diff
     */
    Set < String > getChangedFiles();
    
    /**
     * Retrieve a list of file names (relative to the root
     * under which the diff was taken) modified by this diff,
     * and the type of change associated with each one.
     *  
     * @return mapping of files changed in this diff and the
     * type of modification that occured on each one.
     */
    Map <String, PathChangeType> getChangedFilesStatus();
}

// vi: ai nosi sw=4 ts=4 expandtab

