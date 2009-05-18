/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - Organization for Free and Open Source Software,  *                Athens, Greece.
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

package eu.sqooss.impl.metrics.modulemetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.metrics.modulemetrics.ModuleMetrics;
import eu.sqooss.service.abstractmetric.AbstractMetric;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.abstractmetric.AlreadyProcessingException;
import eu.sqooss.service.abstractmetric.ResultEntry;
import eu.sqooss.service.db.Directory;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectFileMeasurement;
import eu.sqooss.service.db.ProjectFileState;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.ProjectVersionMeasurement;
import eu.sqooss.service.fds.FileTypeMatcher;

public class ModuleMetricsImplementation extends AbstractMetric implements
        ModuleMetrics {

    // Mnemonic names of all metric dependencies
    private static String DEP_WC_LOC = "Wc.loc";

    // Mnemonic names of all supported metrics
    private static String MET_MNOF = "MNOF";
    private static String MET_MNOL = "MNOL";
    private static String MET_ISSRCMOD = "ISSRCMOD"; 
    private static String MET_AMS = "AMS";

    // Holds the instance of the Alitheia core service
    private AlitheiaCore core;

    /**
     * Instantiates a new <code>ModuleMetricsImplementation</code> object.
     * 
     * @param bc the parent bundle's context object
     */
    public ModuleMetricsImplementation(BundleContext bc) {
        super(bc);

        super.addActivationType(ProjectFile.class);
        super.addActivationType(ProjectVersion.class);

        super.addMetricActivationType(MET_MNOF, ProjectFile.class);
        super.addMetricActivationType(MET_MNOL, ProjectFile.class);
        super.addMetricActivationType(MET_AMS, ProjectVersion.class);
        super.addMetricActivationType(MET_ISSRCMOD, ProjectVersion.class);
        
        // Define the plug-in dependencies
        super.addDependency(DEP_WC_LOC);

        // Retrieve the instance of the Alitheia core service
        ServiceReference serviceRef = bc.getServiceReference(
                AlitheiaCore.class.getName());
        if (serviceRef != null)
            core = (AlitheiaCore) bc.getService(serviceRef);
    }

    public boolean install() {
        boolean result = super.install();
        if (result) {
            result &= super.addSupportedMetrics(
                    "Number of Source Code Files in Module",
                    MET_MNOF,
                    MetricType.Type.SOURCE_FOLDER);
            result &= super.addSupportedMetrics(
                    "Mark for modules containing source files",
                    MET_ISSRCMOD,
                    MetricType.Type.SOURCE_FOLDER);
            result &= super.addSupportedMetrics(
                    "Number of lines in module",
                    MET_MNOL,
                    MetricType.Type.SOURCE_FOLDER);
            result &= super.addSupportedMetrics(
                    "Average Module Size",
                    MET_AMS,
                    MetricType.Type.PROJECT_WIDE);
        }
        return result;
    }

    public List<ResultEntry> getResult(ProjectFile pf, Metric m) {
        // Prepare an array for storing the retrieved measurement results
        ArrayList<ResultEntry> results = new ArrayList<ResultEntry>();

        if (!pf.getIsDirectory())
            return null;

        // Search for a matching measurement results
        List<ProjectFileMeasurement> measurement = null;
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("projectFile", pf);
        filter.put("metric", m);
        measurement = db.findObjectsByProperties(
                ProjectFileMeasurement.class, filter);

        // Convert the measurement into a result object
        if (!measurement.isEmpty()) {
            results.add(new ResultEntry(
                    Integer.parseInt(measurement.get(0).getResult()),
                    ResultEntry.MIME_TYPE_TYPE_INTEGER,
                    m.getMnemonic()));
        }

        return results.isEmpty() ? null : results;
    }
 
    public List<ResultEntry> getResult(ProjectVersion pv, Metric m) {
        // Prepare an array for storing the retrieved measurement results
        ArrayList<ResultEntry> results = new ArrayList<ResultEntry>();

        // Search for a matching measurement results
        List<ProjectVersionMeasurement> measurement = null;
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("projectVersion", pv);
        filter.put("metric", m);
        measurement = db.findObjectsByProperties(
                ProjectVersionMeasurement.class, filter);

        // Convert the measurement into a result object
        if (!measurement.isEmpty()) {
            results.add(new ResultEntry(
                    Float.parseFloat(measurement.get(0).getResult()),
                    ResultEntry.MIME_TYPE_TYPE_FLOAT,
                    m.getMnemonic()));
        }

        return results.isEmpty() ? null : results;
    }

    public void run(ProjectFile pf) throws AlreadyProcessingException {
        if (! pf.getIsDirectory()) {
            return;
        }
      
        int mnof = 0;
        int mnol = 0;
        
        List<ProjectFile> pfs = pf.getProjectVersion().getFiles(
                Directory.getDirectory(pf.getFileName(), false), 
                ProjectVersion.MASK_FILES);
        
        boolean foundSource = false; 
        FileTypeMatcher ftm = FileTypeMatcher.getInstance();
        for (ProjectFile f : pfs) {

            if (ftm.getFileType(f.getName()) 
                    != FileTypeMatcher.FileType.SRC) {
                continue;
            }
            // Found one source file, treat the folder as a source module
            mnof++;
            foundSource = true;
            
            // Get the necessary measurement from the Wc.loc metric
            if (ftm.isTextType(f.getName())) {
                mnol += getMeasurement(DEP_WC_LOC, f);
            }
        }
        
        //Only store results for source dirs
        if (foundSource) {
            Metric m = Metric.getMetricByMnemonic(MET_ISSRCMOD);

            ProjectFileMeasurement pfm = new ProjectFileMeasurement(m, pf, String.valueOf(1));
            db.addRecord(pfm);
            markEvaluation(m, pf.getProjectVersion());
            
            m = Metric.getMetricByMnemonic(MET_MNOL);
            pfm = new ProjectFileMeasurement(m, pf,
                    String.valueOf(mnol));
            db.addRecord(pfm);
            markEvaluation(m, pf.getProjectVersion());
            m = Metric.getMetricByMnemonic(MET_MNOF);
            pfm = new ProjectFileMeasurement(m, pf,
                    String.valueOf(mnof));
            db.addRecord(pfm);
            markEvaluation(m, pf.getProjectVersion());
        }
    }

    public void run(ProjectVersion pv) throws AlreadyProcessingException {
        
        String paramIsDirectory = "is_directory";
        String paramMNOL = "paramMNOL";
        String paramISSRCDIR = "paramISSRCDIR";
        String paramVersionId = "paramVersionId";
        String paramProjectId = "paramProjectId";
        String paramState = "paramStatus";

        StringBuffer q = new StringBuffer("select pfm ");
        q.append(" from ProjectVersion pv, ProjectVersion pv2,");
        q.append(" ProjectVersion pv3, ProjectFile pf, ");
        q.append(" ProjectFileMeasurement pfm ");
        q.append(" where pv.project.id = :").append(paramProjectId);
        q.append(" and pv.id = :").append(paramVersionId);
        q.append(" and pv2.project.id = :").append(paramProjectId);
        q.append(" and pv3.project.id = :").append(paramProjectId);
        q.append(" and pf.validFrom.id = pv2.id");
        q.append(" and pf.validUntil.id = pv3.id");
        q.append(" and pv2.sequence <= pv.sequence");
        q.append(" and pv3.sequence >= pv.sequence");
        q.append(" and pf.state <> :").append(paramState);
        q.append(" and pf.isDirectory = :").append(paramIsDirectory);
        q.append(" and pfm.projectFile = pf");
        q.append(" and pfm.metric = :").append(paramMNOL);
        q.append(" and exists (select pfm1 ");
        q.append(" from ProjectFileMeasurement pfm1 ");
        q.append(" where pfm1.projectFile = pfm.projectFile ");
        q.append(" and pfm1.metric = :").append(paramISSRCDIR).append(")");
                
        Map<String,Object> params = new HashMap<String,Object>();
        params.put(paramProjectId, pv.getProject().getId());
        params.put(paramVersionId, pv.getId());
        params.put(paramState, ProjectFileState.deleted());
        params.put(paramIsDirectory, true);
        params.put(paramMNOL, Metric.getMetricByMnemonic(MET_MNOL));
        params.put(paramISSRCDIR, Metric.getMetricByMnemonic(MET_ISSRCMOD));
        
        // Get the list of folders which exist in this project version.
        List<ProjectFileMeasurement> srcDirs = 
            (List<ProjectFileMeasurement>) db.doHQL(q.toString(), params);

        // Calculate the metric results
        int locs = 0;
       
        //For source directory directory
        for (ProjectFileMeasurement pfm : srcDirs) {
            log.debug("Reading measurement for dir " + pfm.getProjectFile().getFileName());
            int mnolValue = Integer.parseInt(pfm.getResult());
            // Try to retrieve the MNOL measurement for this folder
            if (mnolValue > 0)
                locs += mnolValue;
        } 

        if (locs > 0) {
            // Store the "AMS" metric result
            Metric metric = Metric.getMetricByMnemonic(MET_AMS);
            ProjectVersionMeasurement ams = new ProjectVersionMeasurement(
                    metric, pv, String.valueOf(0));
            
            ams.setResult(String.valueOf(((float) (locs / srcDirs.size()))));
            db.addRecord(ams);
            markEvaluation(metric, pv.getProject());
        }
    }
    
    private int getMeasurement(String mnemonic, ProjectFile f) 
        throws AlreadyProcessingException {
        List<Metric> metric = new ArrayList<Metric>();
        AlitheiaPlugin plugin = core.getPluginAdmin().getImplementingPlugin(mnemonic);
        if (plugin == null) {
            log.error("Could not find the " + mnemonic + " metric's plug-in");
        }
        metric.add(Metric.getMetricByMnemonic(mnemonic));
        
        try {
            return plugin.getResult(f, metric).getRow(0).get(0).getInteger();
        } catch (AlreadyProcessingException ape) {
            throw ape;
        } catch (NumberFormatException ex) {
            log.warn("ModuleMetrics: Not an integer: " + ex);
            return 0;
        }
        catch (Exception e) {
            log.error("ModuleMetrics: Results of " + mnemonic
                    + " metric for project: "
                    + f.getProjectVersion().getProject().getName()
                    + ", file: " + f.getFileName() + ", version: "
                    + f.getProjectVersion().getRevisionId()
                    + " could not be retrieved: " + e.getMessage());
            return 0;
        }
    }
}

// vi: ai nosi sw=4 ts=4 expandtab
