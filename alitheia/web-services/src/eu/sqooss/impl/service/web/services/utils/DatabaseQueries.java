/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
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

public class DatabaseQueries {
    
    //5.1.1
    public static final String EVALUATED_PROJECTS_LIST = "select sp, pv " +
                                                         "from StoredProject sp, ProjectVersion pv, Measurement measurement, Metric metric, Plugin plugin " +
                                                         "where sp.id=pv.project " +
                                                         " and pv.id=measurement.projectVersion " +
                                                         " and metric.id=measurement.metric " +
                                                         " and metric.id=plugin.metric " +
                                                         "order by sp.id asc";

    public static final String RETRIEVE_METRICS_4_SELECTED_PPROJECT_PARAM = "project_id";
    
    public static final String RETRIEVE_METRICS_4_SELECTED_PPROJECT = "select metric, metricType " +
                                                                      "from ProjectVersion pv, Measurement measurement, Metric metric, MetricType metricType " +
                                                                      "where pv.id=measurement.projectVersion " +
                                                                      " and metric.id=measurement.metric " +
                                                                      " and metricType.id=metric.metricType " +
                                                                      " and pv.project=:" +
                                                                      RETRIEVE_METRICS_4_SELECTED_PPROJECT_PARAM + " " +
                                                                      "group by metric.id, metric.metricType, metric.description, " +
                                                                      "         metricType.id, metricType.type";
 
    public static final String RETRIEVE_SELECTED_METRIC_PARAM_PR = "project_id";
    
    public static final String RETRIEVE_SELECTED_METRIC_PARAM_METRIC = "metric_id";
    
    public static final String RETRIEVE_SELECTED_METRIC = "select metric, metricType " +
    		                                              "from ProjectVersion pv, Measurement measurement, Metric metric, MetricType metricType " +
    		                                              "where pv.id=measurement.projectVersion " +
    		                                              " and metric.id=measurement.metric " +
    		                                              " and metricType.id=metric.metricType " +
    		                                              " and pv.project=:" +
    		                                              RETRIEVE_SELECTED_METRIC_PARAM_PR + " " +
    		                                              " and metric.id=:" +
    		                                              RETRIEVE_SELECTED_METRIC_PARAM_METRIC + " " +
                                                          "group by metric.id, metric.metricType, metric.description, " +
                                                          "         metricType.id, metricType.type";
    //5.1.1
    
    //5.1.2
    public static final String RETRIEVE_FILE_LIST_PARAM = "project_id";
    
    public static final String RETRIEVE_FILE_LIST = "select pf, fm " +
                                                    "from ProjectVersion pv, ProjectFile pf, FileMetadata fm " +
                                                    "where fm.projectFile=pf.id " +
                                                    " and pf.projectVersion=pv.id " +
                                                    " and pv.project=:" +
                                                    RETRIEVE_FILE_LIST_PARAM;
    //5.1.2
    
}

//vi: ai nosi sw=4 ts=4 expandtab
