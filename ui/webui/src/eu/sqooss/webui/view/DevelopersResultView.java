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

package eu.sqooss.webui.view;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;

import eu.sqooss.webui.Functions;
import eu.sqooss.webui.ListView;
import eu.sqooss.webui.Metric;
import eu.sqooss.webui.Metric.MetricActivator;
import eu.sqooss.webui.Metric.MetricType;
import eu.sqooss.webui.Project;
import eu.sqooss.webui.Result;
import eu.sqooss.webui.datatype.Developer;
import eu.sqooss.webui.util.DevelopersList;

/**
 * The class <code>DevelopersResultView</code> renders an HTML sequence that
 * verbosely presents the metric evaluation results for one or more developers
 * in a specific project. In addition it provides mean for comparing the
 * results against results from other projects (<i>and another set of
 * developers</i>).
 */
public class DevelopersResultView extends ListView {
    /**
     * Holds the project object.
     */
    private Project project;

    /**
     * Hold the list of projects, against which a comparison should be
     * performed.
     */
    private ArrayList<Project> compProjects = new ArrayList<Project>();

    private int chartType = 2;

    /**
     * Instantiates a new <code>DevelopersResultView</code> object, and
     * initializes it with the given project object.
     * 
     * @param project the project object
     */
    public DevelopersResultView(Project project) {
        super();
        this.project = project;
    }

    public void addProject(Project compProject) {
        if ((compProject != null) && (compProject.isValid()))
            this.compProjects.add(compProject);
    }

    public void setChartType(int chartType) {
        this.chartType = chartType;
    }

    /* (non-Javadoc)
     * @see eu.sqooss.webui.ListView#getHtml(long)
     */
    public String getHtml(long in) {
        if ((project == null) || (project.isValid() == false))
            return(sp(in) + Functions.error("Invalid project!"));
        // Holds the accumulated HTML content
        StringBuilder b = new StringBuilder("");
        // Holds the list of currently selected metrics in the main project
        Collection<String> mnemonics =
            project.getSelectedMetrics().getMetricMnemonics(
                    MetricActivator.DEVELOPER,
                    MetricType.PROJECT_WIDE).values();
        // Holds the list of currently selected developers in the main project
        DevelopersList developers = project.getSelectedDevelopers();

        if (developers.isEmpty()) {
            b.append(sp(in) + Functions.warning(
                    "Please, select one or more developers first."));
        }
        else if (mnemonics.isEmpty()) {
            b.append(sp(in) + Functions.warning(
                    "Please, select one or more developer related metrics."));
        }
        else {
            // Retrieve any missing results per developer, if necessary
            for (Developer nextDeveloper : developers) {
                nextDeveloper.setTerrier(this.terrier);
                nextDeveloper.getResults(mnemonics);
            }
            // Construct a per metric table
            b.append(sp(in++) + "<div id=\"table\">\n");
            for (String nextMnemonic : mnemonics) {
                //============================================================
                // Results table
                //============================================================
                b.append(sp(in++) + "<table class=\"borderless\""
                        + " style=\"margin-bottom: 0;\">\n");
                b.append(sp(in++) + "<thead>\n");
                b.append(sp(in++) + "<tr class=\"borderless\">\n");
                Metric metric = project.getEvaluatedMetrics()
                    .getMetricByMnemonic(nextMnemonic);
                String metricLbl = nextMnemonic
                    + ((metric != null)
                            ? " : " + metric.getDescription()
                            : "");
                b.append(sp(in) + "<td class=\"attr\" style=\"width: 70%;\">"
                        + this.adjustRight(metricLbl, "...")
                        + "</td>\n");
                b.append(sp(in) + "<td class=\"borderless\" style=\"width: 30%;\">"
                        + "</td>\n");
                b.append(sp(--in) + "</thead>\n");
                b.append(sp(--in) + "</table>\n");

                String chartFile = null;
                switch (chartType) {
                case 4:
                    chartFile = pieChart(in, nextMnemonic, developers);
                    b.append(sp(in++) + "<table"
                            + " style=\"margin-top: 0;\">\n");
                    b.append(sp(in++) + "</tr>\n");
                    b.append(sp(in) + "<td class=\"chart\">");
                    if (chartFile != null)
                        b.append("<img src=\"/tmp/" + chartFile + "\">");
                    else
                        b.append(Functions.information(
                                "Inapplicable result type."));
                    b.append("</td>\n");
                    b.append(sp(--in) + "</tr>\n");
                    b.append(sp(--in) + "</table>\n");
                    break;
                case 8:
                    chartFile = barChart(in, nextMnemonic, developers);
                    b.append(sp(in++) + "<table"
                            + " style=\"margin-top: 0;\">\n");
                    b.append(sp(in++) + "</tr>\n");
                    b.append(sp(in) + "<td class=\"chart\">");
                    if (chartFile != null)
                        b.append("<img src=\"/tmp/" + chartFile + "\">");
                    else
                        b.append(Functions.information(
                                "Inapplicable result type."));
                    b.append("</td>\n");
                    b.append(sp(--in) + "</tr>\n");
                    b.append(sp(--in) + "</table>\n");
                    break;
                default:
                    b.append(tableChart(in, nextMnemonic, developers));
                    break;
                }
            }
            b.append(sp(--in) + "</div>\n");
        }
        return b.toString();
    }

    public String tableChart(
            long in, String mnemonic, DevelopersList developers) {
        // Holds the accumulated HTML content
        StringBuilder b = new StringBuilder("");
        //------------------------------------------------------------
        // Table header
        //------------------------------------------------------------
        b.append(sp(in++) + "<table"
                + " style=\"margin-top: 0;\">\n");
        b.append(sp(in++) + "<thead>\n");
        b.append(sp(in++) + "<tr class=\"head\">\n");
        b.append(sp(in) + "<td class=\"head\" style=\"width: 50%;\">"
                + "Username</td>\n");
        b.append(sp(in) + "<td class=\"head\" style=\"width: 50%;\">"
                + "Result</td>\n");
        b.append(sp(--in) + "</tr>\n");
        b.append(sp(--in) + "</thead>\n");
        //------------------------------------------------------------
        // Display the metric's result for each developer
        //------------------------------------------------------------
        for (Developer nextDeveloper : developers) {
            b.append(sp(in++) + "<tr>\n");
            b.append(sp(in) + "<td class=\"name\">"
                    + nextDeveloper.getUsername()
                    + "</td>\n");
            Result result =
                nextDeveloper.getResults().get(mnemonic);
            String resultValue = "N/A";
            if (result != null) {
                result.setSettings(this.settings);
                resultValue = result.getHtml(in);
            }
            b.append(sp(in) + "<td>" + resultValue + "</td>\n");
            b.append(sp(--in) + "</tr>\n");
        }
        b.append(sp(--in) + "</table>\n");
        return b.toString();
    }

    public String pieChart(
            long in, String mnemonic, DevelopersList developers) {
        DefaultPieDataset data = new DefaultPieDataset();
        JFreeChart chart;
        // Retrieve the chart values
        Map<String, Double> values = new HashMap<String, Double>();
        for (Developer nextDeveloper : developers) {
            Result result =
                nextDeveloper.getResults().get(mnemonic);
            if (result != null) {
                try {
                    values.put(
                            nextDeveloper.getUsername(),
                            new Double(result.getString()));
                }
                catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        // Generate the chart and save it into a file
        if (values.size() > 0) {
            for (String nextValue : values.keySet())
                data.setValue(nextValue, values.get(nextValue));
            chart = ChartFactory.createPieChart(
                    null, data, true, true, settings.getUserLocale());
            chart.setBackgroundPaint(new Color(0, 0, 0, 0));
            chart.setPadding(RectangleInsets.ZERO_INSETS);
            try {
                File tmpFile = File.createTempFile(
                        "img", "png", settings.getTempFolder());
                ChartUtilities.saveChartAsPNG(tmpFile, chart, 640, 480);
                return tmpFile.getName();
            }
            catch (IOException e) {
                // Do nothing
            }
        }
        return null;
    }

    public String barChart(
            long in, String mnemonic, DevelopersList developers) {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        JFreeChart chart;
        // Retrieve the chart values
        Map<String, Double> values = new HashMap<String, Double>();
        for (Developer nextDeveloper : developers) {
            Result result =
                nextDeveloper.getResults().get(mnemonic);
            if (result != null) {
                try {
                    values.put(
                            nextDeveloper.getUsername(),
                            new Double(result.getString()));
                }
                catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        // Generate the chart and save it into a file
        if (values.size() > 0) {
            for (String nextValue : values.keySet())
                data.addValue(values.get(nextValue), nextValue, nextValue);
            chart = ChartFactory.createBarChart3D(
                    null,
                    "Developer", mnemonic,
                    data, PlotOrientation.VERTICAL,
                    true, true, false);
            chart.setBackgroundPaint(new Color(0, 0, 0, 0));
            chart.setPadding(RectangleInsets.ZERO_INSETS);
            try {
                File tmpFile = File.createTempFile(
                        "img", "png", settings.getTempFolder());
                ChartUtilities.saveChartAsPNG(tmpFile, chart, 640, 480);
                return tmpFile.getName();
            }
            catch (IOException e) {
                // Do nothing
            }
        }
        return null;
    }
}
