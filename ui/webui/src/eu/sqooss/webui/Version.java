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

import java.util.SortedMap;
import java.util.TreeMap;

import eu.sqooss.ws.client.datatypes.WSProjectVersion;

import eu.sqooss.webui.Project;


public class Version extends WebuiItem {

    private static final String COMMENT = "<!-- Version -->\n";
    private Long projectId;
    private Long number;
    //private Long id;
    //private Terrier terrier;
    // Contains a sorted list of all files in this version mapped to their ID.
    //private SortedMap<Long, File> files;

    public Version () {

    }

    public Version (WSProjectVersion wsVersion, Terrier t) {
        id = wsVersion.getId();
        terrier = t;
        number = wsVersion.getVersion();
        projectId = wsVersion.getProject();
    }

    public Version(Long projectId, Long versionId, Terrier t) {
        id = versionId;
        this.projectId = projectId;
        terrier = t;
    }

    public Long getNumber () {
        return number;
    }

    public void setNumber (Long new_number) {
        number = new_number;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long p) {
        projectId = p;
    }

    public SortedMap<Long, File> getFiles () {
        File fs[] = terrier.getProjectVersionFiles(id);
        SortedMap<Long, File> files = new TreeMap<Long, File>();
        if ( fs == null || fs.length == 0 ) {
            files.put(new Long(1337), new File(id, new Long(1337), "src/FakeFile.cpp", "FAKE_STATUS", terrier));
            return files;
        }
        for (File f: fs) {
            files.put(f.getId(), f);
        }
        return files;
    }

    public String getHtml() {
        StringBuilder html = new StringBuilder(COMMENT);
        html.append("<b>Version:</b> " + id);
        return html.toString();
    }

    public String shortName () {
        return "v" + number;
    }

    public String longName () {
        return getHtml(); // Yeah, we'r lazy.
    }
}
