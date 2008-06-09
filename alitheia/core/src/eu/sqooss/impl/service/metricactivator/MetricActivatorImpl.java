/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 Georgios Gousios <gousiosg@gmail.com>
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

package eu.sqooss.impl.service.metricactivator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.InvocationRule;
import eu.sqooss.service.db.InvocationRule.ActionType;
import eu.sqooss.service.db.InvocationRule.ScopeType;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType.Type;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.scheduler.SchedulerException;

public class MetricActivatorImpl implements MetricActivator {

    /** The parent bundle's context object. */
    private BundleContext bc;

    private AlitheiaCore core;
    private Logger logger;
    private PluginAdmin pa;
    private Scheduler sched;
    private DBService db;

    // Default action of the invocation rules chain
    private ActionType defaultAction = null;
    private Long defaultRuleId = null;
    private Long firstRuleId = null;
    private HashMap<Long,InvocationRule> rules = null;

    public MetricActivatorImpl(BundleContext bc, Logger logger) {
        this.bc=bc;

        ServiceReference serviceRef = null;
        serviceRef = bc.getServiceReference(AlitheiaCore.class.getName());
        core = (AlitheiaCore) bc.getService(serviceRef);
        
        this.logger = logger;
        this.pa = core.getPluginAdmin();
        this.sched = core.getScheduler();
        this.db = core.getDBService();
    }

    private void initRules() {
        // Load all defined invocation rules
        //db.startDBSession();
        rules = new HashMap<Long, InvocationRule>();
        InvocationRule defaultRule = InvocationRule.getDefaultRule(db);
        defaultRuleId = defaultRule.getId();
        defaultAction = ActionType.fromString(defaultRule.getAction());
        InvocationRule rule = InvocationRule.first(db);
        firstRuleId = rule.getId();
        while (rule != null) {
            rules.put(rule.getId(), rule);
            rule = rule.next(db);
        }
        //db.commitDBSession();
    }
    
    public void reloadRule (Long ruleId) {
        if(rules == null) {
            initRules();
        }
        
        // Invalid rule Id
        if (ruleId == null) return;
        // Retrieve the target rule from the database
        InvocationRule rule = db.findObjectById(
                InvocationRule.class, ruleId);
        // Rule update
        if (rule != null) {
            rules.put(rule.getId(), rule);
            // Check if this is the first rule in the chain
            if (rule.getPrevRule() == null) {
                firstRuleId = rule.getId();
            }
            // Check if this is the default rule in the chain
            if (rule.getId() == defaultRuleId.longValue()) {
                defaultAction = ActionType.fromString(rule.getAction());
            }
        }
        // Rule remove
        else {
            rules.remove(ruleId);
        }
    }

    public ActionType matchRules (AlitheiaPlugin ap, DAObject resource) {
        if(rules == null) {
            initRules();
        }
        
        // Retrieve the first rule
        InvocationRule rule = rules.get(firstRuleId);

        // Retrieve the plug-in DAO
        Plugin plugin = null;
        if ((ap != null) && (ap.getUniqueKey() != null)) {
            HashMap<String, Object> properties =
                new HashMap<String, Object>();
            List<Plugin> plugins =
                db.findObjectsByProperties(Plugin.class, properties);
            if ((plugins != null) && (plugins.size() > 0)) {
                plugin = plugins.get(0);
            }
        }
        //====================================================================
        // Match against a resource of type ProjectFile
        //====================================================================
        if (resource instanceof ProjectFile) {
            // Skip on project file in state "DELETED"
            if (((ProjectFile) resource).getStatus().equals("DELETED")) {
                return defaultAction;
            }
            ProjectVersion version =
                ((ProjectFile) resource).getProjectVersion();
            StoredProject project = version.getProject();
            // Check for an invalid resource parameters
            if ((project == null)
                    || (plugin == null)
                    || (version == null)) {
                return defaultAction;
            }
            // Traverse through each rule until a match is found.
            while (rule != null) {
                // Check for the default rule
                if (rule.getId() == defaultRuleId) {
                    return defaultAction;
                }
                // Retrieve the rule's metric type
                Type metricType = null;
                if (rule.getMetricType() != null) {
                    metricType = rule.getMetricType().getEnumType();
                }
                // Skip on a metric type which can not evaluate that resource
                if ((metricType != null) 
                        && (metricType != Type.SOURCE_CODE)
                        && (metricType != Type.PROJECT_WIDE)){
                    rule = rules.get(rule.getNextRule());
                    continue;
                }
                // Skip on a different project Id
                if ((rule.getProject() != null)
                        && (rule.getProject().getId() != project.getId())) {
                    rule = rules.get(rule.getNextRule());
                    continue;
                }
                // Skip on a different plug-in Id
                if ((rule.getPlugin() != null)
                        && (rule.getPlugin().getId() != plugin.getId())) {
                    rule = rules.get(rule.getNextRule());
                    continue;
                }
                // Skip on a different metric type
                boolean metricTypeNotFound = true;
                for (Metric metric : ap.getSupportedMetrics()) {
                    metric = db.attachObjectToDBSession(metric);
                    if (metric.getMetricType().getEnumType() == metricType) {
                        metricTypeNotFound = false;
                    }
                }
                if ((metricType != null) && (metricTypeNotFound)) {
                    rule = rules.get(rule.getNextRule());
                    continue;
                }
                // Match against the current rule
                if (rule.match(
                        ScopeType.fromString(rule.getScope()),
                        rule.getValue(),
                        (ProjectFile) resource)) {
                    // TODO: Remove the debug to speed up processing
                    logger.debug("Rule match: "
                            + ((ProjectFile) resource).getFileName()
                            + " : " + rule.getAction());
                    return ActionType.fromString(rule.getAction());
                }
                // Move to the next rule
                rule = rules.get(rule.getNextRule());
            }
        }
        // No matching rule found. Return the default action.
        return defaultAction;
    }

    public <T extends DAObject> void runMetrics(Class<T> clazz,
            SortedSet<Long> objectIDs) {
        // Get a list of all metrics that support the given activation type
        List<PluginInfo> metrics = null;
        metrics = core.getPluginAdmin().listPluginProviders(clazz);
        
        if (metrics == null || metrics.size() == 0) {
            logger.warn("No metrics found for activation type " + clazz.getName());
            return;
        }
        
        // Start a job per processor to schedule metrics
       int cpus = Runtime.getRuntime().availableProcessors();
       int sliceSize = (objectIDs.size() / cpus);
       Long[] entries = objectIDs.toArray(new Long[] {}); 
       
       for (int i = 0; i < cpus; i++) {
           Long [] slice = null;
           if (i < cpus - 1) {
               slice = new Long[sliceSize]; 
               System.arraycopy(entries, i*sliceSize, slice, 0, sliceSize);
           } else {
               int remainder = entries.length - i * sliceSize;
               slice = new Long[remainder];
               System.arraycopy(entries, i*sliceSize, slice, 0, remainder);
           }
           
           MetricActivatorJob maj = new MetricActivatorJob(metrics, clazz,
                    slice, logger, bc);
            
            try {
                sched.enqueue(maj);
            } catch (SchedulerException e) {
                logger.error("Error creating scheduler job:" + e.getMessage());
            }
        }
    }

    public void syncMetric(AlitheiaPlugin m, StoredProject sp) {
        PluginInfo mi = pa.getPluginInfo(m);
        
    }

    public <T extends DAObject> void syncMetrics(StoredProject sp) {
        
    }

    public ProjectVersion getLastAppliedVersion(AlitheiaPlugin m, StoredProject sp) {
        PluginInfo mi = pa.getPluginInfo(m);
        
        Map<String, Object> properties = new HashMap<String, Object>();
     //   mi.
       // List<Metric> metrics = dbs.findObjectsByProperties(Metric.class , properties);
        return null;
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
