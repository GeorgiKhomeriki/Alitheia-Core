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

import eu.sqooss.service.abstractmetric.ResultEntry;

/**
 * The Class WSResultEntry.
 */
public class WSResultEntry {
    
    private String result;
    private String mnemonic;
    private String mimeType;
    private long daoId;
    
    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }
    
    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * @return the mnemonic
     */
    public String getMnemonic() {
        return mnemonic;
    }
    
    /**
     * @param mnemonic the mnemonic to set
     */
    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }
    
    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    /**
     * The method creates a new <code>WSResultEntry</code> object
     * from the existent result entry object.
     * 
     * @param resultEntry -  the result entry object
     * 
     * @return The new <code>WSResultEntry</code> object
     */
    public static WSResultEntry getInstance(ResultEntry resultEntry) {
        if (resultEntry == null) return null;
        try {
            WSResultEntry wsResultEntry = new WSResultEntry();
            wsResultEntry.setMimeType(resultEntry.getMimeType());
            wsResultEntry.setMnemonic(resultEntry.getMnemonic());
            wsResultEntry.setResult(resultEntry.toString());
            return wsResultEntry;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the Id of the DAO to which this measurement belong.
     * 
     * @return the DAO's unique Id
     */
    public long getDaoId() {
        return daoId;
    }

    /**
     * Sets the Id of the DAO to which this measurement belong.
     */
    public void setDaoId(long daoId) {
        this.daoId = daoId;
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
