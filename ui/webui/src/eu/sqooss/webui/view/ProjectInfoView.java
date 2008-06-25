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

import eu.sqooss.webui.Functions;
import eu.sqooss.webui.ListView;
import eu.sqooss.webui.Project;

public class ProjectInfoView extends ListView {
    // Holds the project object
    private Project project;

    // Holds the maximum allowed length of the displayed strings
    private int maxStrLength = 50;

    /**
     * Instantiates a new project info view, and initializes it with the
     * given project object
     * 
     * @param project the project
     */
    public ProjectInfoView(Project project) {
        super();
        this.project = project;
    }

    /**
     * Sets the maximum length of the displayed string variables. Any string
     * which is longer than the specified value will be truncated up to that
     * value.
     * 
     * @param maxStrLength the new maximum string length
     */
    public void setMaxStrLength(int maxStrLength) {
        this.maxStrLength = maxStrLength;
    }

    /**
     * Truncates the specified text string up to the currently set maximum
     * length.
     * 
     * @param text the text string
     * 
     * @return the string
     */
    public String adjustLength (String text) {
        if (text.length() > maxStrLength)
            text = text.substring(0, maxStrLength -1 ) + "...";
        return text;
    }


    public String getHtml(long in) {
        if ((project == null) || (project.isValid() == false))
            return(sp(in) + Functions.error("Invalid project!"));
        StringBuilder html = new StringBuilder();
        html.append(sp(in++) + "<table class=\"projectinfo\">\n");
        // Project website
        html.append(sp(in++) + "<tr>\n");
        html.append(sp(in) + "<td>"
                + "&nbsp;<strong>Website:</strong>"
                + "</td>\n");
        html.append(sp(in) + "<td>"
                + (project.getWebsite() != null
                        ? "<a href=\"" + project.getWebsite() + "\">"
                                + adjustLength(project.getWebsite())
                                + "</a>"
                        : "<i>undefined</i>")
                + "</td>\n");
        html.append(sp(--in) + "</tr>\n");
        // Project contact address
        html.append(sp(in++) + "<tr>\n");
        html.append(sp(in) + "<td>"
                + icon("mail-message-new")
                + "&nbsp;<strong>Contact:</strong>"
                + "</td>\n");
        html.append(sp(in) + "<td>"
                + (project.getContact() != null
                        ? "<a href=\"mailto:" + project.getContact() + "\">"
                                + adjustLength(project.getContact())
                                + "</a>"
                        : "<i>undefined</i>")
                + "</td>\n");
        html.append(sp(--in) + "</tr>\n");
        // Project's source repository
        html.append(sp(in++) + "<tr>\n");
        html.append(sp(in) + "<td>"
                + icon("vcs_status")
                + "&nbsp;<strong>SVN Mirror:</strong>"
                + "</td>\n");
        html.append(sp(in) + "<td>"
                + (project.getRepository() != null
                        ? "<a href=\"files.jsp" + "\">"
                                + adjustLength(project.getRepository())
                                + "</a>"
                        : "<i>undefined</i>")
                + "</td>\n");
        html.append(sp(--in) + "</tr>\n");
        // Project's BTS
        html.append(sp(in++) + "<tr>\n");
        html.append(sp(in) + "<td>"
                + icon("kbugbuster")
                + "&nbsp;<strong>Bug Data:</strong>"
                + "</td>\n");
        html.append(sp(in) + "<td>"
                + (project.getBugs() != null
                        ? adjustLength(project.getBugs())
                        : "<i>undefined</i>")
                + "</td>\n");
        html.append(sp(--in) + "</tr>\n");
        html.append(sp(--in) + "</table>");
        return html.toString();
    }

}
