/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Adriaan de Groot <groot@kde.org>
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.util.Properties;

public class CruncherStatus {
    private class Worker implements Runnable {
        private Object lock;
        private String m;

        public Worker() {
            lock = new Object();
            m = null;
        }

        public void run() {
            String s;
            URL url = null;
            Properties p = new Properties();
            try {
                url = new URL("http://localhost:8088/ws");
            } catch (java.net.MalformedURLException e) {
                synchronized(lock) {
                    m = null;
                }
                // Wait forever
                while(true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }

            while(true) {
                try {
                    URLConnection c = url.openConnection();
                    p.load(c.getInputStream());
                    if (p.getProperty("online").equals("true")) {
                        s = "The cruncher is online, " +
                            p.getProperty("load") + " jobs running, " +
                            p.getProperty("projects") + " projects and " +
                            p.getProperty("metrics") + " metrics available.";
                    } else {
                        s = null;
                    }
                    synchronized(lock) {
                        m = s;
                    }
                } catch (java.io.IOException e) {
                    synchronized(lock) {
                        m = null;
                    }
                }

                // Sleep one minute?
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        public String getM() {
            synchronized(lock) {
                return m;
            }
        }
    }

    private long hits = 0;
    private Worker worker = null;
    private boolean online = false;

    public CruncherStatus() {
        hits = 0;
        online = false;
        Worker w = new Worker();
        Thread t = new Thread(w);
        t.start();
        worker = w;
    }

    public long getHits() {
        return hits;
    }

    private void setHits(long h) {
        hits = h;
    }

    public void hit() {
        ++hits;
    }

    public String getStatus() {
        String s = worker.getM();
        if (s != null) {
            online = true;
            return worker.getM() + " (" + getHits() + " hits)";
        } else {
            online = false;
            return "The cruncher is offline.";
        }
    }

    private void setStatus(String s) {
    }

    public boolean isOnline() {
        return online;
    }

    private void setOnline(boolean b) {
    }
}

// vi: ai nosi sw=4 ts=4 expandtab

