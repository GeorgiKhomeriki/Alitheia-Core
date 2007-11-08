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
 

package eu.sqooss.webui;

import java.util.*;
import eu.sqooss.webui.MetricsListView;

public class MetricsTableView {
    
    Map<Integer,String> metricNames = new HashMap<Integer,String>(); // holds Id, Name
    Map<Integer,String> metricDescriptions = new HashMap<Integer,String>(); // holds Id, description
    
    Integer projectId;

    boolean showId = true;
    boolean showName = true;
    boolean showDescription = true;
    
    boolean showHeader = true;
    boolean showFooter = false;
    
    String tableClass; // CSS class
    String tableId; // identifier in the HTML output
    
    public MetricsTableView () {
        retrieveData();   
    }
    

    public void retrieveData () {
        
        metricNames.put(0, "Line Count");
        metricDescriptions.put(0, "Implements wc -l");
        
        metricNames.put(1, "Cyclic Complexity");
        metricDescriptions.put(1, "How complex is the code?");
        
        metricNames.put(2, "Developer Interaction");
        metricDescriptions.put(2, "Communication between developers");
        
        metricNames.put(3, "Commit Statistics");
        metricDescriptions.put(3, "Number of commits");
        
        metricNames.put(4, "Mailinglist Activity");
        metricDescriptions.put(4, "How many emails have been sent?");
        
    }

    public String getHtml() {
        int columns = 1;
        if (showId) {
            columns++;
        }
        if (showName) {
            columns++;
        }
        if (showDescription) {
            columns++;
        }
        
        String html = new String("<!-- MetricsTableView -->\n<table border=\"1\">\n");
        
        // Table header
        html = html + "<thead><tr>";
        if (showId) {
            html = html + "\n\t<td>ID</td>";
        }
        if (showName) {
            html = html + "\n\t<td>Metric</td>";
        }
        if (showDescription) {
            html = html + "\n\t<td>Description</td>";
        }
        html = html + "</tr></thead>\n\n";

        // Table footer
        if (showFooter) {
            html = html + "<tfoot><tr>";
            
            html = html + "</tr></tfoot>\n\n";
        }
        // Table rows
        html = html + "<tbody>";
        for (Integer key: metricNames.keySet()) {
            html = html + "\n<tr>";
            if (showId) {
                html = html + "\n\t<td>" + key + "</td>";
            }
            if (showName) {
                html = html + "\n\t<td>" + metricNames.get(key) + "</td>";
            }
            if (showDescription) {
                html = html + "\n\t<td>" + metricDescriptions.get(key) + "</td>";
            }
            html = html + "\n</tr>";
        }
        
        html = html + "</tr></tbody>";
        html = html + "\n</table>";

        return html;
    }
    
    public String getHtmlList () {
        String html = new String("<!-- MetricsList -->\n<ul>");
        for (Integer key: metricNames.keySet()) {
            html = html + "\n\t<li>" + metricNames.get(key) + "</li";
        }
        html = html + "\n</ul>";
        return html;
    }
    
    public void setShowId (boolean show) {
        showId = show;
    }
    
    public boolean getShowId () {
        return showId; 
    }
    
    public void setShowName (boolean show) {
        showName = show;
    }
    
    public boolean getShowName () {
        return showName;
    }
    
    public void setShowDescription (boolean show) {
        showDescription = show;
    }
    
    public boolean getShowDescription () {
        return showDescription;   
    }
}