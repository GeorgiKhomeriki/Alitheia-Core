/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Adriaan de Groot <groot@kde.org>
 *
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

package eu.sqooss.impl.service.webadmin;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.logging.LogManagerConstants;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.scheduler.SchedulerStats;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.util.Pair;
import eu.sqooss.service.util.StringUtils;
import eu.sqooss.service.updater.UpdaterService;

import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;

import java.util.Date;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

// Java Extensions
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private long startTime = new Date().getTime();

    private BundleContext bundlecontext = null;

    // AlitheiaCore reference and object
    private ServiceReference srefCore = null;
    private AlitheiaCore sobjAlitheiaCore = null;

    // LogManager and Logger objects
    private LogManager sobjLogManager = null;
    private Logger sobjLogger = null;

    private DBService sobjDB = null;
    private TDSService sobjTDS = null;
    private Scheduler sobjSched = null;    
    private UpdaterService sobjUpdater = null;

    private Hashtable<String,Pair<String,String>> staticContentMap;
    private Hashtable<String,String> dynamicContentMap;
    private Hashtable<String,String> dynamicSubstitutions;

    /**
     * Returns a string representing the uptime of the Alitheia core
     * in dd:hh:mm:ss format
     */
    private String getUptime() {
        long remainder;
        long timeRunning = new Date().getTime() - startTime;

        // Get the elapsed time in days, hours, mins, secs
        int days = new Long(timeRunning / 86400000).intValue();
        remainder = timeRunning % 86400000;
        int hours = new Long(remainder / 3600000).intValue();
        remainder = remainder % 3600000;
        int mins = new Long(remainder / 60000).intValue();
        remainder = remainder % 60000;
        int secs = new Long(remainder / 1000).intValue();

        return String.format("%d:%02d:%02d:%02d", days, hours, mins, secs);
    }

    private void doLogInfo (String logMessage) {
        if (sobjLogger != null) {
            sobjLogger.info(logMessage);
        }
    }

    public void addStaticContent(String path, String type) {
        Pair < String, String > p = new Pair < String, String > (path,type);
        staticContentMap.put(path, p);
    }

    public AdminServlet(BundleContext bc) {
        bundlecontext = bc;

        srefCore = bc.getServiceReference(AlitheiaCore.class.getName());
        if (srefCore != null) {
            sobjAlitheiaCore = (AlitheiaCore) bc.getService(srefCore);
        }
        else {
            System.out.println("No CORE");
        }

        if (sobjAlitheiaCore != null) {
            //Get the LogManager and Logger objects
            sobjLogManager = sobjAlitheiaCore.getLogManager();
            if (sobjLogManager != null) {
                sobjLogger = sobjLogManager.createLogger(
                    LogManagerConstants.loggerNames[10]);
            }

            // Get the DB Service object
            sobjDB = sobjAlitheiaCore.getDBService();
            if (sobjDB != null) {
                doLogInfo("WebAdmin got DB Service object.");
            }

            // Get the TDS Service object
            sobjTDS = sobjAlitheiaCore.getTDSService();
            if (sobjTDS != null) {
                doLogInfo("WebAdmin got TDS Service object.");
            }

            sobjSched = sobjAlitheiaCore.getScheduler();
            if (sobjSched != null) {
                doLogInfo("WebAdmin got Scheduler Service object.");
            }
            
            // Get the Updater Service object
            sobjUpdater = sobjAlitheiaCore.getUpdater();
            if (sobjUpdater != null) {
                doLogInfo("WebAdmin got Updater Service object.");
            }

            Stuffer myStuffer = new Stuffer(sobjDB, sobjLogger, sobjTDS);
            myStuffer.run();

            staticContentMap = new Hashtable < String, Pair < String, String > >();

            // Images and CSS
            addStaticContent("/screen.css", "text/css");
            addStaticContent("/sqo-oss.png", "image/x-png");
            addStaticContent("/queue.png", "image/x-png");
            addStaticContent("/uptime.png", "image/x-png");
            addStaticContent("/greyBack.jpg", "image/x-jpg");
            addStaticContent("/projects.png", "image/x-png");
            addStaticContent("/logs.png", "image/x-png");
            addStaticContent("/bundles.png", "image/x-png");
            addStaticContent("/header-repeat.png", "image/x-png");

            // Pages
            dynamicContentMap = new Hashtable<String,String>();
            dynamicContentMap.put("/about", "/about.html");
            dynamicContentMap.put("/status", "/index.html");
            dynamicContentMap.put("/index", "/index.html");
            dynamicContentMap.put("/projects", "/projects.html");
            dynamicContentMap.put("/logs", "/logs.html");

            dynamicSubstitutions = new Hashtable<String,String>();
        }
    }

    protected String[] getServiceNames(ServiceReference[] servicerefs) {
        if( servicerefs != null ){
            String s, names[] = new String[servicerefs.length];
            int i = 0;

            for (ServiceReference r : servicerefs) {
                Object clazz =
                    r.getProperty( org.osgi.framework.Constants.OBJECTCLASS );
                if (clazz != null) {
                    s = StringUtils.join( (String[])clazz, ", ");
                } else {
                    s = "No class defined";
                }

                names[i++] = s;
            }

            return names;
        } else {
            return null;
        }
    }

    /**
     * Creates an HTML table displaying the key information on
     * bundles and the services that they supply
    */
    protected String renderBundles() {
        if( bundlecontext != null ) {
            Bundle[] bundles = bundlecontext.getBundles();
            String[] statenames = {
                "uninstalled",
                "installed",
                "resolved",
                "starting",
                "stopping",
                "active"
            };

            StringBuilder resultString = new StringBuilder();
            resultString.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n");
            resultString.append("\t<thead>\n");
            resultString.append("\t\t<tr>\n");
            resultString.append("\t\t\t<td>Bundle</td>\n");
            resultString.append("\t\t\t<td>Status</td>\n");
            resultString.append("\t\t\t<td>Services Utilised</td>\n");
            resultString.append("\t\t</tr>\n");
            resultString.append("\t</thead>\n");
            resultString.append("\t<tbody>\n");

            for( Bundle b : bundles ){
                String[] names = getServiceNames(b.getRegisteredServices());

                resultString.append("\t\t<tr>\n\t\t\t<td>");
                resultString.append(StringUtils.makeXHTMLSafe(b.getSymbolicName()));
                resultString.append("</td>\n\t\t\t<td>");
                resultString.append(StringUtils.bitfieldToString(statenames,b.getState()));
                resultString.append("</td>\n\t\t\t<td>\n\t\t\t\t<ul>\n");
                resultString.append(renderList(names));
                resultString.append("\t\t\t\t</ul>\n\t\t\t</td>\n\t\t</tr>\n");
            }

            resultString.append("\t</tbody>\n");
            resultString.append("</table>");

            return resultString.toString();
        } else {
            return null;
        }
    }

    public String renderList(String[] names) {
        if ((names != null) && (names.length > 0)) {
            StringBuilder b = new StringBuilder();
            for (String s : names) {
                b.append("\t\t\t\t\t<li>" + StringUtils.makeXHTMLSafe(s) + "</li>\n");
            }

            return b.toString();
        } else {
            return "\t\t\t\t\t<li>&lt;none&gt;</li>\n";
        }
    }

    /**
    * Sends a resource (stored in the jar file) as a response. The mime-type
    * is set to @p mimeType . The @p path to the resource should start
    * with a / .
    *
    * Test cases:
    *   - null mimetype, null path, bad path, relative path, path not found,
    *   - null response
    *
    * TODO: How to simulate conditions that will cause IOException
    */
    protected void sendResource(HttpServletResponse response, Pair<String,String> source)
        throws ServletException, IOException {
        InputStream istream = getClass().getResourceAsStream(source.first);
        if ( istream == null ) {
            // TODO: Is there a more specific exception?
            throw new IOException( "Path not found: " + source.first);
        }

        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        int totalBytes = 0;

        if ( sobjLogger != null ) {
            sobjLogger.info("Serving " + source.first + " (" + source.second + ")");
        }

        response.setContentType(source.second);
        ServletOutputStream ostream = response.getOutputStream();
        while ( (bytesRead = istream.read(buffer)) > 0 ) {
            ostream.write(buffer,0,bytesRead);
            totalBytes += bytesRead;
        }

        // TODO: Check that the bytes written were as many as the
        //  file size in the JAR (how? it's an InputStream).
        if ( sobjLogger != null ) {
            sobjLogger.info("Wrote " + totalBytes + " from " + source.first);
        }
    }

    protected void sendTemplate(HttpServletResponse response, String path, Hashtable<String,String> subs)
        throws ServletException, IOException {
        BufferedReader istream = new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream(path)));
        if ( istream == null ) {
            // TODO: Is there a more specific exception?
            throw new IOException( "Path not found: " + path );
        }

        if ( sobjLogger != null ) {
            sobjLogger.info("Serving template " + path);
        }

        response.setContentType("text/html");
        PrintWriter print = response.getWriter();
        while ( istream.ready() ) {
            String line = istream.readLine().trim();
            if ( line.startsWith("@@") && subs.containsKey(line) ) {
                print.println(subs.get(line));
            } else {
                print.println(line);
            }
        }
    }

    private void resetSubstitutions() {
        dynamicSubstitutions.clear();
        dynamicSubstitutions.put("@@ABOUT","<p class='box'>This is the administrative interface.</p>");
        dynamicSubstitutions.put("@@STATUS","The cruncher is offline.");
        dynamicSubstitutions.put("@@LOGO","<img src='/logo' id='logo' alt='Logo' />");
        dynamicSubstitutions.put("@@COPYRIGHT","Copyright 2007-2008 <a href=\"http://www.sqo-oss.eu/about/\">SQO-OSS Consortium Members</a>");
        dynamicSubstitutions.put("@@GETLOGS", renderList(sobjLogManager.getRecentEntries()));
        dynamicSubstitutions.put("@@PROJECTS",renderList(listProjects()));
        dynamicSubstitutions.put("@@BUNDLE", renderBundles());
        dynamicSubstitutions.put("@@UPTIME",getUptime());
        dynamicSubstitutions.put("@@QUEUE_LENGTH", String.valueOf(sobjSched.getSchedulerStats().getWaitingJobs()));
        dynamicSubstitutions.put("@@JOB_EXEC", String.valueOf(sobjSched.getSchedulerStats().getRunningJobs()));
        dynamicSubstitutions.put("@@JOB_WAIT", String.valueOf(sobjSched.getSchedulerStats().getWaitingJobs()));
        dynamicSubstitutions.put("@@JOB_WORKTHR", String.valueOf(sobjSched.getSchedulerStats().getWorkerThreads()));
        dynamicSubstitutions.put("@@JOB_FAILED", String.valueOf(sobjSched.getSchedulerStats().getFailedJobs()));
        dynamicSubstitutions.put("@@JOB_TOTAL", String.valueOf(sobjSched.getSchedulerStats().getTotalJobs()));
    }

    private void doServletException(HttpServletRequest request,
        HttpServletResponse response, Throwable e)
        throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter print = response.getWriter();
        print.println("<html><head><title>Alitheia Exception</title></head>");
        print.println("<body>An exception was encountered. The message is:");
        print.println("<blockquote>" + e.getMessage() + "</blockquote>");
        print.println("The stack trace is:");
        print.println("<blockquote>");
        e.printStackTrace(print);
        print.println("</blockquote>");
        print.println("</body></html>");
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        try {
            sobjLogger.info("GET path=" + request.getPathInfo());

            String query = request.getPathInfo();
            if ( (query != null) && (staticContentMap.containsKey(query)) ) {
                sendResource(response, staticContentMap.get(query));
            } else {
                resetSubstitutions();
                if ( (query != null) && dynamicContentMap.containsKey(query) ) {
                    sendTemplate(response,dynamicContentMap.get(query),dynamicSubstitutions);
                } else {
                    sendTemplate(response,"/index.html",dynamicSubstitutions);
                }
            }
        } catch (Exception e) {
            doServletException(request,response,e);
        }
    }

    private String[] listProjects() {
        List l = sobjDB.doHQL("from StoredProject");
        if (l==null) {
            return null;
        }
        String[] results = new String[l.size()];
        for (int i=0; i<l.size(); i++) {
            StoredProject p = (StoredProject) l.get(i);
            results[i] = p.getName() + " (" + p.getWebsite() + ") [id="
                + p.getId() + "]";
        }
        return results;
    }

    private void addProject(HttpServletRequest request) {
        resetSubstitutions();

        String name = request.getParameter("name");
        String website = request.getParameter("website");
        String contact = request.getParameter("contact");
        String bts = request.getParameter("bts");
        String mail = request.getParameter("mail");
        String scm = request.getParameter("scm");

        if ( (name == null) ||
            (website == null) ||
            (contact == null) ||
            (bts == null) ||
            (mail == null) ||
            (scm == null) ) {
            dynamicSubstitutions.put("@@RESULTS","<p>Add project failed because some of the required information was missing.</p>");
            return;
        }

        if (sobjDB != null) {
            StoredProject project = new StoredProject();
            project.setName(name);
            project.setWebsite(website);
            project.setContact(contact);
            project.setBugs(bts);
            project.setRepository(scm);
            project.setMail(mail);
            sobjDB.addRecord(project);

            // Check if the update works
            if (sobjUpdater.update(project, UpdaterService.UpdateTarget.ALL, null)) {
                sobjLogger.info("Added a new project.");
                dynamicSubstitutions.put("@@RESULTS","<p>New project added successfully.</p>");
            }
            else {
                sobjLogger.warn("Adding new project failed.");
                dynamicSubstitutions.put("@@RESULTS","<p>Failed to add new project. " +
                                         "Please check paths to data.");
            }
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                               IOException {
        try {
            sobjLogger.info("POST path=" + request.getPathInfo());
            if ("/addproject".equals(request.getPathInfo())) {
                addProject(request);
                // addProject() has filled in the substitutions by now
                sendTemplate(response,"/results.html",dynamicSubstitutions);
            } else if ("/stop".equals(request.getPathInfo())) {
                dynamicSubstitutions.put("@@RESULTS", "<p>Alitheia Core is now shutdown.</p>");
                sendTemplate(response,"/results.html",dynamicSubstitutions);
                // Stop the system
                bundlecontext.getBundle(0).stop();
            } else if ("/restart".equals(request.getPathInfo())) {
                dynamicSubstitutions.put("@@RESULTS", "<p>Alitheia Core is now restarting. Please wait.</p>");
                startTime = new Date().getTime();
                sendTemplate(response,"/results.html",dynamicSubstitutions);
            } else {
                doGet(request,response);
            }
        } catch (Exception e) {
            doServletException(request,response,e);
        }
    }
}


// vi: ai nosi sw=4 ts=4 expandtab
