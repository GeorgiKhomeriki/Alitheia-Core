/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Sebastian Kuegler <sebas@kde.org>
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

package eu.sqooss.webui;

import eu.sqooss.ws.client.datatypes.WSMetric;

/**
 * This class represents a Metric that has been applied to a project
 * evaluated by Alitheia.
 * It currently only provides access to metric metadata.
 *
 * The Metric class is part of the high-level webui API.
 */
public class Metric extends WebuiItem {

    private String mnemonic;
    private String type;
    private String description;
    private String activationType;

    /** Represents a ProjectFile activated metric. */
    public static final String PROJECT_FILE = "PROJECT_FILE";

    /** Represents a ProjectVersion activated metric. */
    public static final String PROJECT_VERSION = "PROJECT_VERSION";

    /**
     * Constructs a new <code>Metric</code> from a <code>WSMetric</code>
     * instance and a metric type.
     *
     * @param metric the metric object retrieved from the WSS call
     * @param metricType the metric type
     */
    public Metric (WSMetric metric, String metricType) {
        mnemonic    = metric.getMnemonic();
        id          = metric.getId();
        type        = metricType;
        description = metric.getDescription();
        activationType = PROJECT_FILE;
    }

    public Long getId() {
        return id;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getType () {
        // Note that this does not tell us wether we have a ProjectVersion
        // or a ProjectFile metric
        return type;
    }

    public Boolean isProjectVersionMetric () {
        return activationType == PROJECT_VERSION;
    }

    public Boolean isProjectFileMetric () {
        return activationType == PROJECT_FILE;
    }

    public Integer getResult() {
        // FIXME: Do something smart with types, maybe invent a Result class?
        return getResultInteger();
    }

    public Integer getResultInteger() {
        // FIXME: not all results will be 1337, some will also be 42
        return 1337;
    }

    public String getDescription () {
        return description;
    }

    public String getLink() {
        return "<a href=\"/files.jsp?rid=" + getId() + "\">view results</a>";
        // TODO: Should go to results.jsp, with Mnem + ids
    }

    public String getSelectMetricLink() {
        return "<a href=\"/metrics.jsp?selectMetric=" + getId() + "\">Select</a>";
    }

    public String getDeselectMetricLink() {
        return "<a href=\"/metrics.jsp?deselectMetric=" + getId() + "\">Deselect</a>";
    }


    /* (non-Javadoc)
     * @see eu.sqooss.webui.WebuiItem#getHtml(long)
     */
    public String getHtml (long in) {
        return description + " (" + type + ", " + mnemonic + ")";
    }

}
