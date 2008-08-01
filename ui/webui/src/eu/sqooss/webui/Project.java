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

import java.util.*;

import eu.sqooss.webui.datatype.Developer;
import eu.sqooss.webui.util.DevelopersList;
import eu.sqooss.webui.util.MetricsList;
import eu.sqooss.ws.client.datatypes.WSStoredProject;

/**
 * This class represents a project that has been evaluated by the SQO-OSS
 * framework.
 * <br/>
 * It provides access to the project metadata, versions and files.
 * In addition it provides information about the metrics that have been
 * applied to this project and its resources.
 */
public class Project extends WebuiItem {

    /*
     *  Project meta-data fields
     */
    private String bts;
    private String repository;
    private String mail;
    private String contact;
    private String website;
    private long[] developersIds;

    /** Holds the version number of the currently selected version. */
    private Long currentVersionId;

    /** Holds the number of versions in this project. */
    Long versionsCount = null;

    /*
     * Versions cache
     */
    private Version firstVersion = null;
    private Version lastVersion = null;
    private Version currentVersion = null;

    /**
     * A cache for all metrics that have been evaluated on this project.
     */
    private MetricsList metrics = new MetricsList();

    /**
     * A cache for all metrics that have been selected for this project.
     */
    // A cache for all metrics that are selected for this project
    private MetricsList selectedMetrics = new MetricsList();

    /**
     * A cache for all developers that are/were working on this project.
     */
    private DevelopersList developers = new DevelopersList();

    /**
     * A cache for all developers that have been selected for this project.
     */
    private DevelopersList selectedDevelopers = new DevelopersList();

    // Stores all project versions that were retrieved from this object
    private HashMap<Long, Version> versions = new HashMap<Long, Version>();

    /**
     * Instantiates a new <code>Project</code> object, without initializing
     * its data. The object can be initialized later on by calling its
     * <code>initProject()</code> method.
     */
    public Project () {
        setServletPath("/projects.jsp");
        reqParName = "pid";
    }

    /**
     * Instantiates a new <code>Project</code> object, and initializes it with
     * the data stored in the given <code>WStoredProject</code> object.
     * 
     * @param p the project object
     */
    public Project (WSStoredProject p) {
        setServletPath("/projects.jsp");
        reqParName = "pid";
        initProject(p);
   }

    /**
     * Initializes or updates this object with the metadata provided from the
     * given <code>WStoredProject</code> object.
     * 
     * @param p the project object
     */
    private void initProject(WSStoredProject p) {
        if (p != null) {
            id = p.getId();
            name = p.getName();
            bts = p.getBugs();
            repository = p.getRepository();
            mail = p.getMail();
            contact = p.getContact();
            website = p.getWebsite();
            developersIds = p.getDevelopers();
        }
    }

    /**
     * Copies the target object's metadata into this object.
     * 
     * @param p the target <code>Project</code> instance
     */
    public void copy (Project p) {
        if (p != null) {
            id = p.getId();
            name = p.getName();
            bts = p.getBugs();
            repository = p.getRepository();
            mail = p.getMail();
            contact = p.getContact();
            website = p.getWebsite();
            developersIds = p.getDevelopersIds();
        }
    }

    /**
     * Gets the project's homepage.
     * 
     * @return the project's homepage, or <code>null</code> when the
     *   project is not yet initialized.
     */
    public String getWebsite () {
        return website;
    }

    /**
     * Gets the project's contact email
     * 
     * @return the project's contact email, or <code>null</code> when the
     *   project is not yet initialized.
     */
    public String getContact () {
        return contact;
    }

    /**
     * Gets the project's mailing list.
     * 
     * @return the project's mailing list, or <code>null</code> when the
     *   project is not yet initialized.
     */
    public String getMail () {
        return mail;
    }

    /**
     * Gets the project's BTS URL.
     * 
     * @return the project's BTS URL, or <code>null</code> when the
     *   project is not yet initialized.
     */
    public String getBugs() {
        return bts;
    }

    /**
     * Gets the project's source repository.
     * 
     * @return the project's source repository, or <code>null</code> when the
     *   project is not yet initialized.
     */
    public String getRepository() {
        return repository;
    }

    public long[] getDevelopersIds() {
        return developersIds;
    }

    //========================================================================
    // DATA RETRIEVAL METHODS
    //========================================================================

    /**
     * Retrieves the version with the given Id from the SQO-OSS framework,
     * unless the local cache contains that version already.
     * 
     * @param versionId the version Id
     * 
     * @return the version object, or <code>null</code> if a version with the
     *   given Id doesnÄt exist for this project.
     */
    public Version getVersion (Long versionId) {
        Version result = null;
        // Check in the versions cache first
        if (versions.containsKey(versionId))
            result = versions.get(versionId);
        // Otherwise retrieve it from the SQO-OSS framework
        else if (terrier != null) {
            result = terrier.getVersion(this.getId(), versionId);
            if (result != null)
                versions.put(result.getId(), result);
        }
        return result;
    }

    /**
     * Retrieves the list of all metrics that have been evaluated on this
     * project from the SQO-OSS framework, unless the local cache contains
     * some data already.
     * 
     * @return the list of metrics evaluated on this project, or an empty
     *   list when none are found or the project not yet initialized.
     */
    public MetricsList getEvaluatedMetrics() {
        if (terrier == null)
            return metrics;
        if (isValid()) {
            if (metrics.isEmpty()) {
                metrics.addAll(terrier.getMetricsForProject(id));
                selectAllMetrics();
            }
        }
        else
            terrier.addError("Invalid project!");
        return metrics;
    }

    /**
     * Retrieves the list of all developers that are/were working on this
     * project from the SQO-OSS framework, unless the local cache contains
     * some data already.
     * 
     * @return the list of developers working on this project, or an empty
     *   list when none are found or the project not yet initialized.
     */
    public DevelopersList getDevelopers() {
        if (terrier == null)
            return developers;
        if (isValid()) {
            if ((developersIds != null) && (developers.isEmpty())) {
                developers.addAll(terrier.getDevelopers(developersIds));
            }
        }
        return developers;
    }

    /**
     * Returns the total number of developers in this project
     *
     * @return Total number of developers in this project.
     */
    public long getDevelopersCount() {
        return getDevelopers().size();
    }

    /**
     * Retrieves all the data that is required by this object from the
     * SQO-OSS framework, unless the cache contains some data already
     * or this project is not yet initialized.
     * 
     * @param terrier the <code>Terrier<code> instance
     */
    public void retrieveData(Terrier terrier) {
        if (terrier == null)
                return;
        if (isValid()) {
            setTerrier(terrier);
            getEvaluatedMetrics();
            getDevelopers();
        }
        else
            terrier.addError("Invalid project!");
    }

    /**
     * Flushes all the data that is cached by this object.
     */
    public void flushData() {
        currentVersionId = null;
        versionsCount = null;
        firstVersion = null;
        lastVersion = null;
        currentVersion = null;
        versions.clear();
        metrics.clear();
        developers.clear();
        selectedMetrics = new MetricsList();
    }

    /**
     * Flushes the list of evaluated metrics that is cached by this object.
     */
    public void flushMetrics() {
        metrics.clear();
    }

    //========================================================================
    // VERSION RELATED METHODS
    //========================================================================

    /**
     * Sets the first version of this project.
     * 
     * @param version the version object
     */
    public void setFirstVersion(Version version) {
        if (version != null)
            firstVersion = version;
    }

    /**
     * Gets the first known version of this project.
     *
     * @return the first version's object, or <code>null<code> if the project
     *   has no versions at all.
     */
    public Version getFirstVersion() {
        if (isValid()) {
            if ((firstVersion == null) && (terrier != null))
                firstVersion = terrier.getFirstProjectVersion(getId());
        }
        else
            terrier.addError("Invalid project!");
        return firstVersion;
    }

    /**
     * Sets the last version of this project.
     * 
     * @param version the version object
     */
    public void setLastVersion(Version version) {
        if (version != null)
            lastVersion = version;
    }

    /**
     * Gets the last known version of this project.
     *
     * @return the last version's object, or <code>null</code> if the project
     *   has no versions at all.
     */
    public Version getLastVersion() {
        if (isValid()) {
            if ((lastVersion == null) && (terrier != null))
                lastVersion = terrier.getLastProjectVersion(getId());
        }
        else
            terrier.addError("Invalid project!");
        return lastVersion;
    }

    /**
     * Sets the current version.
     *
     * @param version the version object
     */
    public void setCurrentVersion(Version version) {
        if ((version != null) && (version.isValid())){
            currentVersion = version;
            currentVersionId = version.getId();
        }
    }

    /**
     * Gets the current version.
     *
     * @return The version object.
     */
    public Version getCurrentVersion() {
        if (currentVersion == null)
            currentVersion = getLastVersion();
        return currentVersion;
    }

    /**
     * Sets the version with the given number as current.
     * 
     * @param versionNumber the version number
     */
    public void setCurrentVersionId(Long versionNumber) {
        currentVersionId = versionNumber;
    }

    /**
     * Returns the number of the current version.
     *
     * @return the version number, or <code>null</code> if a current version
     *   is not yet defined.
     */
    public Long getCurrentVersionId() {
        try {
            if (currentVersionId == null) {
                return getLastVersion().getId();
            }
            return currentVersionId;
        } catch (NullPointerException e) {
            terrier.addError("Could not retrieve current version.");
            return null;
        }
    }

    /**
     * Returns the total number of versions in this project
     *
     * @return Total number of versions in this project.
     */
    public long getVersionsCount() {
        if (versionsCount == null)
            versionsCount = terrier.getVersionsCount(id);
        if (versionsCount != null)
            return versionsCount;
        else
            return 0;
    }

    //========================================================================
    // METRIC SELECTION METHODS
    //========================================================================

    /**
     * Adds the metric with the given Id to the list of metrics that are
     * selected for this project.
     * 
     * @param id the metric Id
     */
    public void selectMetric (Long id) {
        if (id != null) {
            Metric metric = metrics.getMetricById(id);
            if (metric != null) selectedMetrics.add(metric);
        }
    }

    /**
     * Removes the metric with the given Id from the list of metrics that were
     * selected for this project.
     * 
     * @param id the metric Id
     */
    public void deselectMetric (Long id) {
        if (id != null) {
            Metric metric = metrics.getMetricById(id);
            if (metric != null) selectedMetrics.remove(metric);
        }
    }

    /**
     * Adds all evaluated metrics to the list of selected metrics.
     */
    public void selectAllMetrics () {
        for (Metric metric : metrics)
            if (selectedMetrics.contains(metric) == false)
                selectedMetrics.add(metric);
    }

    /**
     * Cleans up the list of selected metrics.
     */
    public void deselectAllMetrics () {
        selectedMetrics.clear();
    }

    /**
     * Returns the list of all metrics that were selected for this project.
     * 
     * @return the list of selected metrics.
     */
    public MetricsList getSelectedMetrics() {
        return selectedMetrics;
    }

    //========================================================================
    // DEVELOPER SELECTION METHODS
    //========================================================================

    /**
     * Adds the developer with the given Id to the list of developers that are
     * selected for this project.
     * 
     * @param id the developer Id
     */
    public void selectDeveloper (Long id) {
        if (id != null) {
            Developer developer = developers.getDeveloperById(id);
            if (developer != null) selectedDevelopers.add(developer);
        }
    }

    /**
     * Removes the developer with the given Id from the list of developers
     * that were selected for this project.
     * 
     * @param id the developer Id
     */
    public void deselectDeveloper (Long id) {
        if (id != null) {
            Developer developer = developers.getDeveloperById(id);
            if (developer != null) selectedDevelopers.remove(developer);
        }
    }

    /**
     * Adds all project developers to the list of selected developers.
     */
    public void selectAllDevelopers () {
        for (Developer developer : developers)
            if (selectedDevelopers.contains(developer) == false)
                selectedDevelopers.add(developer);
    }

    /**
     * Cleans up the list of selected developers.
     */
    public void deselectAllDevelopers () {
        selectedDevelopers.clear();
    }

    /**
     * Returns the list of all developers that were selected for this project.
     * 
     * @return the list of selected developers.
     */
    public DevelopersList getSelectedDevelopers() {
        return selectedDevelopers;
    }

    //========================================================================
    // RESULTS RENDERING METHODS
    //========================================================================

    /* (non-Javadoc)
     * @see eu.sqooss.webui.WebuiItem#getHtml(long)
     */
    public String getHtml(long in) {
        // Holds the accumulated HTML content
        StringBuilder html = new StringBuilder("");

        if (isValid())
            html.append(sp(in) + "<h2>" + getName() + "</h2>");
        else
            html.append(sp(in) + Functions.error("Invalid project!"));

        return html.toString();
    }

    //========================================================================
    // IMPLEMENTATIONS OF REQUIRED java.lang.Object METHODS
    //========================================================================

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object target) {
        if (this == target) return true;
        if (target == null) return false;
        if (getClass() != target.getClass()) return false;
        Project project = (Project) target;
        if (getId().equals(project.getId()))
            if (getName().equals(project.getId()))
                return true;
        return false;
    }

}
