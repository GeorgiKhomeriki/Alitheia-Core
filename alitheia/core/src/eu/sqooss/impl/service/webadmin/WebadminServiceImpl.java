/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 by Adriaan de Groot <groot@kde.org>
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

package eu.sqooss.impl.service.webadmin;

import java.util.Hashtable;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import eu.sqooss.service.webadmin.WebadminService;

public class WebadminServiceImpl implements WebadminService {
    private ServiceReference serviceref = null;
    private HttpService httpservice = null;
    private AdminServlet servlet = null;

    public WebadminServiceImpl(BundleContext bc) throws Exception {
        serviceref = bc.getServiceReference("org.osgi.service.http.HttpService");
        if (serviceref != null) {
            httpservice = (HttpService) bc.getService(serviceref);
            servlet = new AdminServlet(bc);
            httpservice.registerServlet("/", (Servlet) servlet,
                                        new Hashtable(), null);
            httpservice.registerServlet("/ws", (Servlet) new AdminWS(bc),
                                        new Hashtable(), null);
        } else {
            System.out.println("! Could not load the HTTP service.");
        }
    }

    public String[] getConfigurationKeys() {
        return null;
    }

    public String getConfigurationProperty(String key) {
        return key;
    }

    public void setConfigurationProperty(String key, String val) {
    }

    // Perform a self-test
    public Object selfTest() {
        return null;
    }
}


// vi: ai nosi sw=4 ts=4 expandtab

