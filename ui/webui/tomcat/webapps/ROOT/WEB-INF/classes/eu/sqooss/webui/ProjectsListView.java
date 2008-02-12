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


import eu.sqooss.webui.ListView;
import eu.sqooss.scl.WSException;
import eu.sqooss.scl.WSSession;
import eu.sqooss.scl.result.WSResult;


public class ProjectsListView extends ListView {

    String currentProject;
    WSResult result;
    String someresult;

    public ProjectsListView () {
        retrieveData();
    }

    public String giveMeSomeData() {
        return "Status: " + someresult;
    }

    public void setCurrentProject ( String project ) {
        currentProject = project;
    }

    public String getCurrentProject () {
        String pid = String.valueOf(getCurrentProjectId());
        return currentProject + " (" + pid + ")";
    }

    public int getCurrentProjectId() {
        return getProjectId(currentProject);
    }

    public void retrieveData () {
        try {
            result = session.getConnection().evaluatedProjectsList();//.next().get(0).getLong() + "=";
        } catch (WSException wse) {
            error += "<br />Something went wrong getting evaluatedProjectsList() ... :/";
        }
        /*
        items.addElement(new String("FreeBSD"));
        items.addElement(new String("Apache"));
        items.addElement(new String("KDE"));
        items.addElement(new String("Samba"));
        items.addElement(new String("Nmap"));
        currentProject = "KDE";
        */
    }

    Integer getProjectId (String project) {
        // FIXME: This should not just return the index of a static list :-)
        /*
         * int i = 0;
        for (String item: items) {
            if (project.equals(item)) {
                return i;
            }
            i++;
        }
        */
        return -1; // Means: not found, maybe an exception here?
    }
}