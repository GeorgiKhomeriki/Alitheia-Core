/*
 * Copyright (c) Members of the SQO-OSS Collaboration, 2007
 * All rights reserved by respective owners.
 * See http://www.sqo-oss.eu/ for details on the copyright holders.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the SQO-OSS project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package eu.sqooss.vcs;

import java.util.*;

public abstract class FileEntry {

    private HashMap<String, String> attributes;

    private String fullPath;

    EntryKind kind;

    private String name;

    String revision;

    int size;

    public enum EntryKind {
	Unknown, File, Dir
    }

    public enum InputDataFormat {
	Plain, Svn, SvnXml, SvnLogXml
    }

    public FileEntry(String name) {
	this.name = name;
	fullPath = "";
	attributes = new HashMap<String, String>();
	kind = EntryKind.Unknown;
    }

    public String getName() {
	return name;
    }

    void setName(String name) {
	this.name = name;
    }

    public String getFullPath() {
	return fullPath;
    }

    void setFullPath(String fullPath) {
	this.fullPath = fullPath;
    }

    public HashMap<String, String> getAttributes() {
	return attributes;
    }

    public int getSize() {
	return size;
    }

    void setSize(int size) {
	this.size = size;
    }

    public String getRevision() {
	return revision;
    }

    void setRevision(String revision) {
	this.revision = revision;
    }

    public EntryKind getKind() {
	return kind;
    }

    void setKind(EntryKind kind) {
	this.kind = kind;
    }

    public static EntryKind parseEntryKind(String kind) {
	String k = kind.toLowerCase();

	if (k == "file") {
	    return EntryKind.File;
	}

	if (k == "dir") {
	    return EntryKind.Dir;
	}

	return EntryKind.Unknown;
    }
}
