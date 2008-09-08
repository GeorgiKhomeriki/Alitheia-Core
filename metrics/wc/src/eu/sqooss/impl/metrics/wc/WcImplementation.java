/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 Georgios Gousios <gousiosg@gmail.com>
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

package eu.sqooss.impl.metrics.wc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.metrics.wc.Wc;
import eu.sqooss.service.abstractmetric.AbstractMetric;
import eu.sqooss.service.abstractmetric.ResultEntry;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectFileMeasurement;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.fds.FileTypeMatcher;

public class WcImplementation extends AbstractMetric implements Wc {
    
    private FDSService fds;
	
    private static final String MNEMONIC_WC_LOC   = "Wc.loc";
    private static final String MNEMONIC_WC_LOCOM = "Wc.locom";
    private static final String MNEMONIC_WC_LONB  = "Wc.lonb";
    private static final String MNEMONIC_WC_WORDS = "Wc.words";
    
    private HashMap<String,String[]> commentDelimiters;
    
    public WcImplementation(BundleContext bc) {
        super(bc);
        super.addActivationType(ProjectFile.class);
        
        super.addMetricActivationType(MNEMONIC_WC_LOC, ProjectFile.class);
        super.addMetricActivationType(MNEMONIC_WC_LOCOM, ProjectFile.class);
        super.addMetricActivationType(MNEMONIC_WC_LONB, ProjectFile.class);
        super.addMetricActivationType(MNEMONIC_WC_WORDS, ProjectFile.class);
        
        ServiceReference serviceRef = null;
        serviceRef = bc.getServiceReference(AlitheiaCore.class.getName());
       
        
        fds = ((AlitheiaCore)bc.getService(serviceRef)).getFDSService();
        
        commentDelimiters = new HashMap<String,String[]>(10);
        // Fill up the comment delimiters hash with a collection
        // of delimiters for various languages.
        addCommentDelimiters("cpp|C|cc|java",new String[]{"//","/\\*","\\*/"});
        addCommentDelimiters("c",new String[]{null,"/\\*","\\*/"});
        addCommentDelimiters("py|sh",new String[]{"#",null,null});
        addCommentDelimiters("html",new String[]{null,"<!--","-->"});
    }

    public boolean install() {
        boolean result = super.install();
        if (result) {
            result &= super.addSupportedMetrics(
                    "Lines of Code",
                    MNEMONIC_WC_LOC,
                    MetricType.Type.SOURCE_CODE);
            result &= super.addSupportedMetrics(
                    "Lines of Comments", 
                    MNEMONIC_WC_LOCOM,
                    MetricType.Type.SOURCE_CODE);
            result &= super.addSupportedMetrics(
                    "Non-blank lines", 
                    MNEMONIC_WC_LONB,
                    MetricType.Type.SOURCE_CODE);
            result &= super.addSupportedMetrics(
                    "Total words", 
                    MNEMONIC_WC_WORDS,
                    MetricType.Type.SOURCE_CODE);
        }
        return result;
    }

    public List<ResultEntry> getResult(ProjectFile a, Metric m) {
        
        ArrayList<ResultEntry> results = new ArrayList<ResultEntry>();
        // Search for a matching project file measurement
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("projectFile", a);
        filter.put("metric", m);
        List<ProjectFileMeasurement> measurement =
            db.findObjectsByProperties(ProjectFileMeasurement.class, filter);
    	return convertMeasurements(measurement,m.getMnemonic());
    }

    public void run(ProjectFile pf) {
        // We do not support directories
        if (pf.getIsDirectory()) {
            return;
        }
        
        //We don't support binary files either
        if (FileTypeMatcher.getFileType(pf.getName()).equals(
                FileTypeMatcher.FileType.BIN)) {
            return;
        }

        String extension = FileTypeMatcher.getFileExtension(pf.getName());
        String delimiters[] = commentDelimiters.get(extension);
        if (null == delimiters) {
            delimiters = commentDelimiters.get("c");
        }
        
        InputStream in = fds.getFileContents(pf);
        if (in == null) {
            return;
        }
        // Create an input stream from the project file's content
        try {
            log.info(this.getClass().getName() + " Measuring: "
                    + pf.getFileName());
            
            /* Match start of multiline comment */
            String startMultiLine = delimiters[1];
           
            /* End multiline comment */
            String endMultiLine = delimiters[2];
            
            /* Match single line comments, C/Java/C++ style*/
            String singleLine = delimiters[0];
            
            Pattern singleLinePattern = null;
            if (null != singleLine) {
                singleLinePattern = Pattern.compile(singleLine);
            }
            
            // Measure the number of lines in the project file
            LineNumberReader lnr = 
                new LineNumberReader(new InputStreamReader(in));
            int comments = 0;
            int non_blank = 0;
            int words = 0;
            // The count of the number of lines is stored in the
            // line number reader itself.

            MultiLineMatcher mlm = null;
            if (null != startMultiLine) {
                mlm = new MultiLineMatcher(startMultiLine,endMultiLine);
            }
            String line = null;
            while ((line = lnr.readLine()) != null) {
                // Count non-blank lines
                if (line.trim().length()>0) {
                    non_blank++;
                }
                
                // Count words -- the tokenizer is not the best approach
                words += new StringTokenizer(line).countTokens();

                // First we check for multi-line comments, then
                // for single liners if we have not already counted
                // the line as a comment.
                if ((null != mlm) && mlm.checkLineForComment(line)) {
                    comments++;
                } else {
                    if (null != singleLinePattern) {
                        
                        // Find single-line comments
                        Matcher m = singleLinePattern.matcher(line);
                        /* Single line comments */
                        if (m.find()) {
                            comments++;
                        }
                    }
                }
            }
            
            lnr.close();

            // Store the results
            Metric metric = Metric.getMetricByMnemonic(MNEMONIC_WC_LOC);
            ProjectFileMeasurement locm = new ProjectFileMeasurement(
                    metric,pf,String.valueOf(lnr.getLineNumber()));
            db.addRecord(locm);
            markEvaluation(metric, pf);
            
            metric = Metric.getMetricByMnemonic(MNEMONIC_WC_LOCOM);
            ProjectFileMeasurement locc = new ProjectFileMeasurement(
                    metric,pf,String.valueOf(comments));
            db.addRecord(locc);
            markEvaluation(metric, pf);
            
            metric = Metric.getMetricByMnemonic(MNEMONIC_WC_LONB);
            ProjectFileMeasurement lonb = new ProjectFileMeasurement(
                    metric,pf,String.valueOf(non_blank));
            db.addRecord(lonb);
            markEvaluation(metric, pf);
            
            metric = Metric.getMetricByMnemonic(MNEMONIC_WC_WORDS);
            ProjectFileMeasurement words_measure = new ProjectFileMeasurement(
                    metric,pf,String.valueOf(words));
            db.addRecord(words_measure);
            markEvaluation(metric, pf);
        } catch (IOException e) {
            log.error(this.getClass().getName() + " IO Error <" + e
                    + "> while measuring: " + pf.getFileName());
        
        }
    }

    /**
     * For a list of file extensions, register the three delimiters
     * as the single-line and multi-line comment delimiters. For the
     * comment matching, this will be used to power the multi-line
     * matcher and the single-line comment matchers.
     * 
     * @param extensions String listing file extensions separated by |
     * @param delimiters Three-element array of delimiter regexps; any
     *          one or more of these may be null.
     */
    private void addCommentDelimiters(String extensions, String[] delimiters) {
        if (delimiters.length != 3) {
            log.error("The number of delimiters for languages <" + extensions + "> is wrong (must be 3)");
            return;
        }
        
        String[] l = extensions.split("|");
        for (String e : l) {
            commentDelimiters.put(e,delimiters);
        }
    }
    

    private class MultiLineMatcher {
        private Pattern startRE;
        private Pattern endRE;
        private boolean inside;
        
        MultiLineMatcher(String start, String end) {
            // It doesn't make sense to have null patterns
            // either for start or end, so don't check for null.
            this.startRE = Pattern.compile(start);
            this.endRE = Pattern.compile(end);
            this.inside = false;
        }
        
        public boolean checkLineForComment(String line) {
            // If we are inside at the start of the line, then
            // this is a comment line, regardless. Otherwise,
            // this will be counted as a comment only if a comment
            // starts on this line.
            boolean r = inside;
            
            Matcher m_start = startRE.matcher(line);
            Matcher m_end = endRE.matcher(line);
            
            // Set up a two-element array where toggle[0]
            // is the next relevant token to be looking for.
            // This is start if we're outside, and end if
            // we're inside a comment right now.
            Matcher toggle[] = { m_start, m_end } ;
            if (inside) {
                toggle[0] = m_end;
                toggle[1] = m_start;
            }
            
            // Looking from point forward in the line,
            // we keep looking for the next (toggle[0])
            // relevant token.
            int point = 0;
            while (toggle[0].find(point)) {
                // If we found it, advance point,
                // note that this line was a comment 
                // (if we were inside, then it already was;
                // and if we were outside, then we've just moved
                // inside. Change states from in to out or vice-versa.
                point=toggle[0].start()+1;
                r = true;
                inside = !inside;
                // Swap around the two relevant matchers
                Matcher temp = toggle[0];
                toggle[0]=toggle[1];
                toggle[1]=temp;
            }
            
            return r;
        }
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
