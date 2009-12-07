/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
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

package eu.sqooss.impl.service.web.services.datatypes;

/**
 * This class wraps the <code>eu.sqooss.service.db.ProjectVersion</code>.
 */
public class WSVersionStats {

    private long versionId;
    private long deletedCount;
    private long modifiedCount;
    private long addedCount;

    /**
     * @return The version Id.
     */
    public long getVersionId() {
        return versionId;
    }

    /**
     * @param id the version Id
     */
    public void setVersionId(long id) {
        this.versionId = id;
    }
    

    /**
     * @return The number of files deleted in this version.
     */
    public long getDeletedCount() {
        return deletedCount;
    }

    /**
     * @param number the number of files deleted in this version
     */
    public void setDeletedCount(long number) {
        this.deletedCount = number;
    }

    /**
     * @return The number of files modified in this version.
     */
    public long getModifiedCount() {
        return modifiedCount;
    }

    /**
     * @param number the number of files modified in this version
     */
    public void setModifiedCount(long number) {
        this.modifiedCount = number;
    }

    /**
     * @return The number of files added in this version.
     */
    public long getAddedCount() {
        return addedCount;
    }

    /**
     * @param number the number of files added in this version
     */
    public void setAddedCount(long number) {
        this.addedCount = number;
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
