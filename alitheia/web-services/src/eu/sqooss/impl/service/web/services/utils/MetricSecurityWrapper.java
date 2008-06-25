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

package eu.sqooss.impl.service.web.services.utils;

import eu.sqooss.service.db.DBService;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.security.SecurityManager;

public class MetricSecurityWrapper extends AbstractSecurityWrapper{
    
    public MetricSecurityWrapper(SecurityManager security, DBService db, Logger logger) {
        super(security, db, logger);
    }

    public boolean checkMetricTypesReadAccess(String userName,
            String password, long[] metricTypesIds) {
        synchronized (privilegesLockObject) {
            privileges.clear();
            for (long metricTypeId : metricTypesIds) {
                privileges.put(Privilege.METRICTYPE_READ.toString(),
                        Long.toString(metricTypeId));
            }
            return security.checkPermission(ServiceUrl.DATABASE.toString(),
                    privileges, userName, password);
        }
    }
    
    public boolean checkMetricsReadAccess(String userName, String password) {
        synchronized (privilegesLockObject) {
            privileges.clear();
            privileges.put(Privilege.METRIC_READ.toString(),
                    PrivilegeValue.ALL.toString());
            return security.checkPermission(ServiceUrl.PLUGINADMIN.toString(),
                    privileges, userName, password);
        }
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
