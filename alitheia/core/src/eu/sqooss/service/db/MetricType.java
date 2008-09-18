/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Paul J. Adams <paul.adams@siriusit.co.uk>
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

package eu.sqooss.service.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DAObject;

/**
 * Instances of this class represent to what forms of data a metric
 * stored in the database is related to
 */
public class MetricType extends DAObject {
    /**
     * A string representation of the type of metric
     */
    private String type;
    
    /**
     * A list of all metrics of this type
     */
    private Set<Metric> metrics;

/**
 * An enumeration of the metric types:
 * <ul>
 * <li>SOURCE_CODE - Relates to SVN source files</li>
 * <li>SOURCE_FOLDER - Relates to SVN source folders</li>
 * <li>MAILING_LIST - Relates to email data</li>
 * <li>BUG_DATABASE - Relates to BTS data</li>
 * <li>PROJECT_WIDE - Relates to all available data</li>
 *</ul>
 */
    public enum Type {

        SOURCE_CODE, SOURCE_FOLDER, MAILING_LIST, BUG_DATABASE, PROJECT_WIDE;

        public static Type fromString(String s) {
            if ("SOURCE_CODE".equals(s))
                return Type.SOURCE_CODE;
            if ("SOURCE_FOLDER".equals(s))
                return Type.SOURCE_FOLDER;
            else if ("MAILING_LIST".equals(s))
                return Type.MAILING_LIST;
            else if ("BUG_DATABASE".equals(s))
                return Type.BUG_DATABASE;
            else if ("PROJECT_WIDE".equals(s))
                return Type.PROJECT_WIDE;
            else
                return null;
        }
    }

    public MetricType() {
        // Nothing to do here
    }

    public MetricType(Type t) {
        type = t.toString();
    }

    public Type getEnumType() {
        return Type.fromString(type);
    }

    public String getType() {
        return type;
    }
    
    public void setEnumType(Type type) {
        this.type = type.toString();
    }

    public void setType(String s) {
        this.type = Type.fromString(s).toString();
    }
    
    public Set<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }

    /**
     * Get the corresponding DAO for the provided metric type
     * @param t
     * @return A MetricType DAO representing the metric type
     */
    public static MetricType getMetricType(Type t) {
        DBService db = AlitheiaCore.getInstance().getDBService();
        HashMap<String, Object> s = new HashMap<String, Object>();
        s.put("type", t.toString());
        List<MetricType> result = db.findObjectsByProperties(MetricType.class, s);
        if (result.isEmpty()) {
            return null;
        }
        else {
            return result.get(0);
        }
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
