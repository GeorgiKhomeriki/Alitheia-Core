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

package eu.sqooss.impl.service.web.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.sqooss.impl.service.web.services.datatypes.WSMetric;
import eu.sqooss.impl.service.web.services.datatypes.WSMetricsResultRequest;
import eu.sqooss.impl.service.web.services.datatypes.WSResultEntry;
import eu.sqooss.impl.service.web.services.utils.MetricManagerDatabase;
import eu.sqooss.impl.service.web.services.utils.SecurityWrapper;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.abstractmetric.MetricMismatchException;
import eu.sqooss.service.abstractmetric.Result;
import eu.sqooss.service.abstractmetric.ResultEntry;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.security.SecurityManager;

public class MetricManager extends AbstractManager {
    
    private Logger logger;
    private PluginAdmin pluginAdmin;
    private MetricManagerDatabase dbWrapper;
    private SecurityWrapper securityWrapper;
    
    public MetricManager(Logger logger, DBService db,
            PluginAdmin pluginAdmin, SecurityManager security) {
        super(db);
        this.logger = logger;
        this.pluginAdmin = pluginAdmin;
        this.dbWrapper = new MetricManagerDatabase(db);
        this.securityWrapper = new SecurityWrapper(security);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getMetricsByProjectId(String, String, long)
     */
    public WSMetric[] getMetricsByProjectId(String userName,
            String password, long projectId) {
        
        logger.info("Retrieve metrics for selected project! user: " + userName +
                "; project id:" + projectId);
        
        securityWrapper.checkProjectReadAccess(userName, password, projectId);
        
        super.updateUserActivity(userName);
        
        List<?> metrics = dbWrapper.getMetricsByProjectId(projectId);
        return convertToWSMetrics(metrics);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getMetricsByFileNames(String, String, String, String[], String[])
     */
    public WSMetric[] getMetricsByFileNames(String userName, String password,
            long projectId, String[] folders, String[] fileNames) {
        logger.info("Retrieve metrics for selected files! user: " + userName + "; project id: " + projectId);

        securityWrapper.checkProjectReadAccess(userName, password, projectId);

        super.updateUserActivity(userName);
        
        Set<String> fileNamesSet;
        if ((fileNames.length == 0) || (fileNames[0] == null)) {
            fileNamesSet = new HashSet<String>();
        } else {
            fileNamesSet = new HashSet<String>(Arrays.asList(fileNames));
        }
        
        if ((folders.length != 0) && (folders[0] != null)) {
            Map<String, Object> folderNameParameters = new Hashtable<String, Object>(1);
            List currentFileNames;
            for (String folder : folders) {
                currentFileNames = dbWrapper.getFilesFromFolder(projectId, folder);
                fileNamesSet.addAll(currentFileNames);
            }
        }
        
        List<?> result = null;
        
        if (fileNamesSet.size() != 0) {
            result = dbWrapper.getMetricsByFileNames(projectId, fileNamesSet);
        }
        
        return convertToWSMetrics(result);
    }
    
    /**
     * @see eu.sqooss.service.web.services.WebServices#getMetrics(String, String)
     */
    public WSMetric[] getMetrics(String userName, String password) {
        logger.info("Get metrics! user: " + userName);
        
        securityWrapper.checkMetricsReadAccess(userName, password, null);
        
        super.updateUserActivity(userName);
        
        return convertToWSMetrics(dbWrapper.getMetrics());
    }
    
    @SuppressWarnings("unchecked")
    public WSResultEntry[] getMetricsResult(String userName, String password,
            WSMetricsResultRequest resultRequest) {
        logger.info("Get metrics result! user: " + userName +
                "; request: " + resultRequest.toString());
        
        securityWrapper.checkMetricsReadAccess(userName, password, resultRequest.getMnemonics());
        
        super.updateUserActivity(userName);
        
        WSResultEntry[] result = null;
        List<WSResultEntry> resultList = null;
        DAObject daObject = dbWrapper.getMetricsResultDAObject(resultRequest);
        if (daObject != null) {
            List<Metric> metrics = (List<Metric>) dbWrapper.getMetricsResultMetricsList(resultRequest);
            resultList = getMetricsResult(metrics, daObject);
        }
        if ((resultList != null) && (resultList.size() != 0)) {
            result = new WSResultEntry[resultList.size()];
            resultList.toArray(result);
        }
        return result;
    }
    
    private List<WSResultEntry> getMetricsResult(List<Metric> metrics, DAObject daObject) {
        if ((metrics == null) || (metrics.size() == 0) || (daObject == null)) {
            return null;
        }
        List<WSResultEntry> resultList = new ArrayList<WSResultEntry>();
        Hashtable<AlitheiaPlugin, List<Metric>> plugins = groupMetricsByPlugins(metrics);
        AlitheiaPlugin currentPlugin;
        Result currentResult = null;
        List<Metric> currentPluginMetrics;
        for (Enumeration<AlitheiaPlugin> keys = plugins.keys(); keys.hasMoreElements(); /*empty*/) {
            currentPlugin = keys.nextElement();
            currentPluginMetrics = plugins.get(currentPlugin);
            try {
                currentResult = currentPlugin.getResult(daObject, currentPluginMetrics);
            } catch (MetricMismatchException e) {
                currentResult = null;
            }
        }
        if (currentResult != null) {
            List<ResultEntry> currentRow;
            for (int i = 0; i < currentResult.getRowCount(); i++) {
                currentRow = currentResult.getRow(i);
                for (int j = 0; j < currentRow.size(); j++) {
                    resultList.add(new WSResultEntry(currentRow.get(j)));
                }
            }
        }
        return resultList;
    }
    
    private Hashtable<AlitheiaPlugin, List<Metric>>  groupMetricsByPlugins(List<Metric> metrics) {
        Hashtable<AlitheiaPlugin, List<Metric>> plugins =
            new Hashtable<AlitheiaPlugin, List<Metric>>();
        if ((metrics != null) && (metrics.size() != 0)) {
            AlitheiaPlugin currentPlugin = null;
            for (Metric metric : metrics) {
                currentPlugin = pluginAdmin.getImplementingPlugin(
                        metric.getMnemonic());
                if (currentPlugin != null) {
                    if (plugins.containsKey(currentPlugin)) {
                        plugins.get(currentPlugin).add(metric); 
                    } else {
                        List<Metric> metricList = new ArrayList<Metric>(1);
                        metricList.add(metric);
                        plugins.put(currentPlugin, metricList);
                    }
                }
            }
        }
        return plugins;
    }
    
    private WSMetric[] convertToWSMetrics(List<?> metrics) {
        WSMetric[] result = null;
        if ((metrics != null) && (metrics.size() != 0)) {
            result = new WSMetric[metrics.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = new WSMetric((Metric) metrics.get(i));
            }
        }
        return result;
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
