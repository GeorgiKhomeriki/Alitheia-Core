/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2008 by Sebastian Kuegler <sebas@kde.org>
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

import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import eu.sqooss.scl.WSException;
import eu.sqooss.ws.client.datatypes.WSMetric;
import eu.sqooss.ws.client.datatypes.WSMetricType;
import eu.sqooss.ws.client.datatypes.WSMetricsRequest;
import eu.sqooss.ws.client.datatypes.WSProjectFile;
import eu.sqooss.ws.client.datatypes.WSProjectVersion;
import eu.sqooss.ws.client.datatypes.WSStoredProject;
import eu.sqooss.ws.client.datatypes.WSUser;
import eu.sqooss.ws.client.datatypes.WSMetricsResultRequest;
import eu.sqooss.ws.client.datatypes.WSResultEntry;

/**
 * This class is the entry point for retrieving data from the
 * Alitheia core through the webservices. It has a connection
 * to the core and exposes data query methods.
 */
public class Terrier {
    private String error = "";
    private String debug = "";

    // Points to the the WebUI's configuration bundle
    private ResourceBundle confParams;

    private TerrierConnection connection = null;

    private Project currentProject;

    /**
     * Empty constructor. Instantiates a new <code>Terrier</code> object.
     */
    public Terrier () {
    }

    /**
     * Loads the <code>Terrier</code>'s configuration setting from the
     * specified <code>ResourceBundle</code> object. In the exceptional case,
     * when the specified configuration object is invalid, this function will
     * fall back to a set of predefined configuration settings.
     *
     * @param configuration a <code>ResourceBunde</code> configuration object
     */
    public void initConfig(ResourceBundle configuration) {
        this.confParams = configuration;

        String userName = Constants.conUserName;
        String userPass = Constants.conUserPass;
        String connUrl  = Constants.conConnURL;

        if (confParams != null) {
            if (confParams.getString(Constants.cfgUnprivUser) != null) {
                userName = confParams.getString(Constants.cfgUnprivUser);
            }
            if (confParams.getString(Constants.cfgUnprivPass) != null) {
                userPass = confParams.getString(Constants.cfgUnprivPass);
            }
            if (confParams.getString(Constants.cfgFrameworkURL) != null) {
                connUrl = confParams.getString(Constants.cfgFrameworkURL);
            }
        }

        // Instantiate an object for connecting to SQO-OSS
        connection = new TerrierConnection(connUrl, userName, userPass);
    }

    public TerrierConnection connection() {
        return connection;
    }

    /**
     * Retrieves descriptive information about the selected project from
     * the SQO-OSS framework, and constructs a Project object from it.
     *
     * @param projectId The ID of the selected project.
     * @return A Project object.
     */
    public Project getProject(Long projectId) {
        if (!connection.isConnected()) {
            return null;
        }
        debug += "ok";

        Project prj;
        WSStoredProject[] storedProjects;
        try {
            // Retrieve information about this project
            storedProjects = connection.getProjectAccessor().getProjectsByIds(new long[] {projectId});
            if (storedProjects.length != 0) {
                prj = new Project(storedProjects[0], this);
            } else {
                error = "The project does not exist!";
                prj = null;
            }
        } catch (WSException wse) {
            error = "Could not retrieve the project:" + wse.getMessage();
            prj = null;
        }
        return prj;
    }

    public void setCurrentProject(Project p) {
        currentProject = p;
    }

    /**
     * Gets the list of all project that were evaluated in the attached
     * SQO-OSS framework.
     *
     * @return The list of evaluated projects.
     */
    public Vector<Project> getEvaluatedProjects() {
        Vector<Project> projects = new Vector<Project>();
        if (!connection.isConnected()) {
            addError(connection.getError());
            return projects;
        }
        try {
            // Retrieve evaluated projects only
            WSStoredProject projectsResult[] =
                connection.getProjectAccessor().getEvaluatedProjects();

            for (WSStoredProject wssp : projectsResult) {
                projects.addElement(new Project(wssp, this));
            }
        } catch (WSException wse) {
            addError("Can not retrieve the list of evaluated projects.");
            return projects;
        }
        return projects;
    }

    /**
     * Fetch a Version by project and versionId.
     *
     * @param projectId The ID of selected project
     * @param projectId The ID of the version
     * @return The Version matching projectId and versionId.
     */
    public Version getVersion(Long projectId, Long versionId) {
        if (!connection.isConnected()) {
            addError(connection.getError());
            addError("Not connected.");
            return null;
        }
        try {
            // Retrieve evaluated projects only
            WSProjectVersion[] versionsResult =
                connection.getProjectAccessor().getProjectVersionsByIds(new long[] {versionId});
            if (versionsResult.length > 0) {
                Version v = new Version(versionsResult[0], this);
                return v;
            } else {
                return null;
            }
        } catch (WSException wse) {
            addError("Cannot this version " + versionId + "for project " + projectId + ".");
        }
        return null;
    }

    /**
     * Retrieves all metrics that has been evaluated for the selected
     * projects, and generates a proper view for displaying them.
     *
     * @param projectId The ID of selected project
     * @return The corresponding view object
     */
    public MetricsTableView getMetrics4Project(Long projectId) {
        if (!connection.isConnected()) {
            return null;
        }
        MetricsTableView view = new MetricsTableView(projectId);
        try {
            WSMetric[] metrics =
                connection.getMetricAccessor().getProjectEvaluatedMetrics(projectId);
            for (WSMetric met : metrics) {
                view.addMetric(new Metric(met, this));
            }
        } catch (WSException wse) {
            addError("Can not retrieve the list of metrics for this project:" + wse.getMessage());
            return null;
        }
        return view;
    }

    public WSMetric[] getMetricsForProject(Long projectId) {
        try {
            return connection.getMetricAccessor().getProjectEvaluatedMetrics(projectId);
        } catch (WSException wse) {
            return null;
        }
    }

    /**
     * Retrieves all metrics and generates a proper view for displaying them.
     *
     * @return The corresponding view object
     */
    public MetricsTableView getAllMetrics() {
        if (!connection.isConnected()) {
            addError(connection.getError());
            return null;
        }
        MetricsTableView metricTableView = new MetricsTableView();
        try {
            WSMetricsRequest request = new WSMetricsRequest();
            request.setSkipResourcesIds(true);
            request.setIsFileGroup(true);
            request.setIsProjectFile(true);
            request.setIsProjectVersion(true);
            request.setIsStoredProject(true);
            WSMetric[] allMetrics = connection.getMetricAccessor().getMetricsByResourcesIds(request);
            for (WSMetric wsMetric : allMetrics) {
                metricTableView.addMetric(new Metric(wsMetric, this));
            }
        } catch (WSException wse) {
            error = "Cannot retrieve the list of all metrics." + wse.getMessage();
            return null;
        }
        return metricTableView;
    }

    /**
     * Retrieves all files that exist in the specified project version,
     * and generates a proper view for displaying them.
     *
     * FIXME: This returns a listview and should not be in terrier.
     *
     * @param versionId The ID of selected project version
     * @return The corresponding view object
     */
    public FileListView getFiles4ProjectVersion(Long versionId) {
        if (!connection.isConnected()) {
            return null;
        }
        FileListView view = new FileListView();
        try {
            try {
                WSProjectFile[] files = connection.getProjectAccessor().getFilesByProjectVersionId(versionId);
                for (WSProjectFile file : files) {
                    view.addFile(new File(file, this));
                }
        } catch (NullPointerException e) {
            // Nevermind?
        }
        } catch (WSException e) {
            error = "Can not retrieve the list of files for this version.";
            return null;
        }
        return view;
    }

    public Result[] getVersionResults (Long[] ids) {
        try {
            // prepare ResultRequester for file retrieval
            WSMetricsResultRequest request = new WSMetricsResultRequest();
            request.setDaObjectId(ids[0].longValue()); // FIXME: Use array here, also pending API change
            request.setProjectVersion(true);
            // Retrieve results from the accessor
            WSResultEntry[] wsresults = connection.getMetricAccessor().getMetricsResult(request);
            Result[] results = new Result[wsresults.length];
            // create Array and return it
            for (int i = 0; i < results.length; i++) {
                results[i] = new Result(wsresults[i], this);
            }
            return results;
        } catch (WSException wse) {
            addError("Failed to retrieve ProjectResults.");
        }
        Result[] results = new Result[0];
        return results;
    }

    public Result[] getFileResults (Long[] ids) {

        try {
            // prepare Metrics Result Requester
            WSMetricsResultRequest request = new WSMetricsResultRequest();
            request.setDaObjectId(ids[0].longValue()); // FIXME: Use array here, also pending API change
            request.setProjectFile(true);
            String[] mnemonics = new String[1];
            mnemonics[0] = "LOC";
            request.setMnemonics(mnemonics);
            // Retrieve results from the accessor
            WSResultEntry[] wsresults = connection.getMetricAccessor().getMetricsResult(request);
            Result[] results = new Result[wsresults.length];
            // create Array and return it
            for (int i = 0; i < results.length; i++) {
                results[i] = new Result(wsresults[i], this);
            }
            return results;
        } catch (WSException wse) {
            addError("Failed to retrieve FileResult.");
        }
        Result[] results = new Result[0];
        return results;
    }

    public Vector<File> getProjectVersionFiles(Long versionId) {
        if (!connection.isConnected()) {
            return null;
        }
        Vector<File> files = new Vector<File>();
        try {
            try {
                WSProjectFile[] wsfiles = connection.getProjectAccessor().getFilesByProjectVersionId(versionId);
                for (WSProjectFile file : wsfiles) {
                    files.addElement(new File(file, this));
                    //addError("gPVF:" + file.getId());
                }
            } catch (NullPointerException npe) {
                addError("NPE looping files:" + npe.getMessage());
                // Nevermind.
            }
        } catch (WSException e) {
            addError("Can not retrieve the list of files for this version:" + e.getMessage());
            return files;
        }
        //addError(files.size() + " Files n Version " + versionId);
        return files;
    }

    /**
     * Retrieves the number of all files that exist in the specified project
     * version.
     *
     * @param versionId The ID of selected project version
     * @return The number of files.
     */
    public Long getFilesNumber4ProjectVersion(Long versionId) {
        if (!connection.isConnected()) {
            return null;
        }
        try {
            return connection.getProjectAccessor().getFilesNumberByProjectVersionId(versionId);
        } catch (WSException e) {
            addError("Can not retrieve the number of files for this version.");
        }
        return null;
    }

    public Version getLastProjectVersion(Long projectId) {
        long[] v = new long[1];
        v[0] = projectId.longValue();
        try {
            WSProjectVersion[] wsversions = connection.getProjectAccessor().getLastProjectVersions(v);
            if (wsversions.length != 0) {
                return new Version(wsversions[0], this);
            } else {
                return null;
            }
        } catch (WSException e) {
            addError("Can not retrieve last project version.");
            return null;
        }
    }

    public Metric getMetric(Long metricId) {
        // TODO
        return null;
    }

    public String getMetricTypeById(long metricTypeId) {
        if (!connection.isConnected()) {
            return null;
        }
        String result = null;
        try {
            WSMetricType[] metricTypes = connection.getMetricAccessor().
            getMetricTypesByIds(new long[] {metricTypeId});
            if (metricTypes.length != 0) {
                result = metricTypes[0].getType();
            } else {
                error = "The metric type doesn't exist!";
            }
        } catch (WSException e) {
            error = "Can not retrieve information about the metric type.";
        }
        return result;
    }

    /**
     * Retrieves information about the specified user from the SQO-OSS
     * framework.
     *
     * @param userId the user's account Id
     *
     * @return an User object holding information about the requested user, or
     * <code>null</code> when no information is available
     */
    public User getUserById (Long userId) {
        if (!connection.isConnected()) {
            return null;
        }
        try {
            WSUser[] users = connection.getUserAccessor().getUsersByIds(new long[] {userId});
            if (users.length != 0) {
                return new User(users[0].getId(), users[0].getUserName(), users[0].getEmail());
            } else {
                error = "The user does not exist!";
            }
        } catch (WSException e) {
            error = "Can not retrieve information about the selected user.";
        }
        return null;
    }

    /**
     * Retrieves information about the specified user from the SQO-OSS
     * framework.
     *
     * @param userName the user's name
     *
     * @return an User object holding information about the requested user, or
     * <code>null</code> when no information is available
     */
    public User getUserByName (String userName) {
        if (!connection.isConnected()) {
            return null;
        }
        try {
            WSUser user = connection.getUserAccessor().getUserByName(userName);
            if (user != null) {
                return new User(user.getId(), user.getUserName(), user.getEmail());
            }
        } catch (WSException e) {
            error = "Can not retrieve information about the selected user.";
        }
        return null;
    }

    public File getFile(Long fileId) {
        // TODO
        return null;
    }

    /**
     * The Alitheia core may have a message-of-the-day stored in it,
     * which is then printed when the user hits the front page.
     */
    public String getUserMessageOfTheDay() {
        try {
            return connection.getUserAccessor().getMessageOfTheDay();
        } catch (WSException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Adds a (pending) user to the connected SQO-OSS system.
     */
    public boolean registerUser (
            String username,
            String password,
            String email) {
        if (!connection.isConnected()) {
            return false;
        }
        try {
            return connection.getUserAccessor().createPendingUser(username, password, email);
        } catch (WSException e) {
            error = "An error occured during the registration process!";
            error += " Please try again later.";
            return false;
        }
    }

    /**
     * Forwarding function to TerrierConnection.loginUser
     */
    public boolean loginUser(String user, String pass) {
        return connection.loginUser(user,pass);
    }

    /**
     * Forwarding function to TerrierConnection.logoutUser
     */
    public void logoutUser(String user) {
        connection.logoutUser(user);
    }

    public String getError() {
        return "\n<ul>" + error + "\n</ul>";
    }

    public void addError(String message) {
        error += "\n\t<li>" + message + "</li>";
    }

    public void flushError() {
        error = "";
    }

    public String getDebug() {
        return debug;
    }
}
