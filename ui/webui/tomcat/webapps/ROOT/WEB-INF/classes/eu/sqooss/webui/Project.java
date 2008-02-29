/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008-2008 by Sebastian Kuegler <sebas@kde.org>
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

import java.util.ArrayList;
import eu.sqooss.ws.client.datatypes.WSStoredProject;


public class Project {

    private Long    id;
    private String  name;
    private String  bts;
    private String  repository;
    private String  mail;
    private String  contact;
    private String  website;
    private Long    versionLow;
    private Long    versionHigh;

    public Project () {
        
    }

    public Project (WSStoredProject p) {
        id = p.getId();
        name = p.getName();
        bts = p.getBugs();
        repository = p.getRepository();
        mail = p.getMail();
        contact = p.getContact();
        website = p.getWebsite();
    }

    public Long getId () {
        return id;
    }

    public String getName () {
        return name;
    }

    public String getWebsite () {
        return website;
    }

    public String getMail () {
        return mail;
    }

    public String getContact () {
        return contact;
    }

    public String getBts() {
        return bts;
    }

    public String getRepository() {
        return repository;
    }

    public Long getVersionLow () {
        return versionLow;
    }

    public void setVersionLow (Long version) {
        this.versionLow = version;
    }

    public Long getVersionHigh () {
        return versionHigh;
    }

    public void setVersionHigh (Long version) {
        this.versionHigh = version;
    }

    public String getInfo() {
        StringBuilder html = new StringBuilder();
        html.append("\n<table>\n\t<tr>\n\t\t<td>");
        html.append("Website: \n\t\t</td><td>\n"
                + (getWebsite() != null 
                        ? "<a href=\"" + getWebsite() + "\">" + getWebsite() + "</a>"
                        : "<i>undefined</i>"));
        html.append("\n\t\t</td>\n\t</tr>\n\t<tr>\n\t\t<td>");
        html.append("Contact: \n\t\t</td><td>\n"
                + (getContact() != null 
                        ? "<a href=\"" + getContact() + "\">" + getContact() + "</a>"
                        : "<i>undefined</i>"));
        html.append("\n\t\t</td>\n\t</tr>\n\t<tr>\n\t\t<td>");
        html.append("SVN Mirror: \n\t\t</td><td>\n"
                + (getRepository() != null 
                        ? "<a href=\"files.jsp" + getId() + "\">" + getRepository() + "</a>"
                        : "<i>undefined</i>"));
        html.append("\n\t\t</td>\n\t</tr>\n\t<tr>\n\t\t<td>");
        html.append("Bug Tracking System: \n\t\t</td><td>\n"
                + (getBts() != null 
                        ? "<a href=\"" + getBts() + "\">" + getBts() + "</a>"
                        : "<i>undefined</i>"));
        html.append("\n\t\t</td>\n\t</tr>\n</table>");
        return html.toString();
    }

    public String getHtml() {
        StringBuilder html = new StringBuilder("<!-- Project -->\n");
        html.append("<h2>" + getName() + " (" + getId() + ")</h2>");
        html.append(getInfo());
        return html.toString();
    }
}
