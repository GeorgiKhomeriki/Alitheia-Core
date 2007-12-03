/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 * 
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 by Georgios Gousios <gousiosg@aueb.gr>
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

package eu.sqooss.service.updater;

public interface UpdaterService {

    /**
     * Targets for an update request
     *
     */
    public enum UpdateTarget {
        /* Request to update source code metadata*/
        SOURCE_CODE_DATA("code"),
        /* Request to update repository metadata*/
        REPOSITORY_DATA("repo"),
        /* Request to update mailing list metadata*/
        MAILING_LIST_DATA("mail"),
        /* Request to update bug metadata*/
        BUG_DATABASE_DATA("bug"),
        /* Request to update all metadata*/
        ALL("all");
        
        private String desc;
        
        private UpdateTarget(String desc) {
            this.desc = desc;
        }

        public static UpdateTarget fromString(String s) {
            
            if (s == null)
                return null;
            
            for ( UpdateTarget t : values() ) {
        	if(s.contains(t.desc)) { return t; }
            }
            
            return null;
        }         
    }

    /**
     * Inform the updater service about changes to the data mirrors
     * 
     * @param project - The project name that has been updated
     * @param target - Specifies which project resource has been updated
     */
    void update(String project, UpdateTarget target);
}
