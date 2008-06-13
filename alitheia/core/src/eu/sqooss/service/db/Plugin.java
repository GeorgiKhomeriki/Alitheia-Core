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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.sqooss.impl.service.CoreActivator;

/**
 * Instances of this class represent data related to Alitheia Core
 * plugins, stored in the database
 */
public class Plugin extends DAObject{
    /**
     * the name of the plugin
     */
    private String name; 

    /**
     * A representation of date on which the plugin was installed into
     * the ALitheia Core
     */
    private Date installdate; 
    
    /**
     * The version number of this plugin
     */
    private String version; 

    /**
     * A description of the plugin and the metrics provided
     */
    private String description; 

    /**
     * Denotes if the metric is active as well as being installed
     */
    private boolean active;

    /**
     * A hashcode representing the plugin bundle to ensure a unique
     * identifier
     */
    private String hashcode;

    /**
     * A list of all configuration entries for this plugin
     */
    private Set<PluginConfiguration> configurations = new HashSet<PluginConfiguration>();
    
    /**
     * A list of all supported metrics for this plugin
     */
    private Set<Metric> supportedMetrics = new HashSet<Metric>();
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getInstalldate() {
        return installdate;
    }

    public void setInstalldate(Date installdate) {
        this.installdate = installdate;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }
    
    public Set<PluginConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Set<PluginConfiguration> configurations) {
        this.configurations = configurations;
    }

    public Set<Metric> getSupportedMetrics() {
        return supportedMetrics;
    }

    public void setSupportedMetrics(Set<Metric> supportedMetrics) {
        this.supportedMetrics = supportedMetrics;
    }

    public static List<Plugin> getPluginByName(String name) {
        DBService db = CoreActivator.getDBService();
        HashMap<String, Object> s = new HashMap<String, Object>();
        s.put("name", name);
        return db.findObjectsByProperties(Plugin.class, s);
    }
    
    /**
     * Get Plugin by hashcode
     * 
     * @param hashcode
     *                The object's hashcode for the plugin class that implements
     *                the
     *                {@link eu.sqooss.service.abstractmetric.AlitheiaPlugin}
     *                interface
     * @return A Plugin object if the hashcode was found in the DB; null
     *         otherwise
     */
    public static Plugin getPluginByHashcode(String hashcode) {
        DBService db = CoreActivator.getDBService();
        HashMap<String, Object> s = new HashMap<String, Object>();
        s.put("hashcode", hashcode);
        List<Plugin> l = db.findObjectsByProperties(Plugin.class, s); 
        if (!l.isEmpty())
            return l.get(0);
        
        return null;
    }
}

//vi: ai nosi sw=4 ts=4 expandtab

