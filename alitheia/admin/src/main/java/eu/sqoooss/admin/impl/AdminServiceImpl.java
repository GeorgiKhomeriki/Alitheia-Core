/*
 * Copyright 2010 - Organization for Free and Open Source Software,  
 *                 Athens, Greece.
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

package eu.sqoooss.admin.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import eu.sqooss.admin.AdminAction;
import eu.sqooss.admin.AdminAction.AdminActionStatus;
import eu.sqooss.admin.AdminService;

/**
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 *
 */
@Path("/api")
public class AdminServiceImpl extends Thread implements AdminService {
    
    Map<String, Class<? extends AdminAction>> services;
    ConcurrentMap<Long, ActionContainer> liveactions;
    AtomicLong id;
    
    public AdminServiceImpl() {
        services = new HashMap<String, Class<? extends AdminAction>>();
        liveactions = new ConcurrentHashMap<Long, ActionContainer>();
        id = new AtomicLong();
        start();
    }
    
    @Override
    public void registerAdminAction(String uniq,
            Class<? extends AdminAction> clazz) {
        services.put(uniq, clazz);
    }

    @Override
    public void execute(AdminAction a) {
        a.execute();
    }
    
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/actions/")
    @Override
    public Set<AdminAction> getAdminActions() {
        return new HashSet(services.values());
    }
    
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/actions/{id}")
    public AdminAction show(Long id) {
        if (liveactions.get(id) != null)
            return liveactions.get(id).aa;
        return null;
    }
    
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/actions/{id}/result")
    public Map<String, Object> result() {
        if (liveactions.get(id) == null)
            return null;
        
        if (liveactions.get(id).end == -1)
            return null;
        
        return liveactions.get(id).aa.results();
    }
    
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/actions/{id}/status")
    public AdminActionStatus status() {
        if (liveactions.get(id) != null)
            return liveactions.get(id).aa.getStatus();
        
        if (liveactions.get(id).end == -1)
            return null;
        return null;
    }
    
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/actions/{id}/error")
    public Map<String, Object> error() {
        if (liveactions.get(id) != null)
            return liveactions.get(id).aa.results();
        return null;
    }
    
    @POST
    @Produces({"application/xml", "application/json"})
    @Path("/actions/{uniq}")
    public AdminAction create(String uniq) {
        Class<? extends AdminAction> clazz = services.get(uniq);
        
        if (clazz == null)
            return null;
        
        try {
            AdminAction aa = clazz.newInstance();
            ActionContainer ac = new ActionContainer(aa, id.addAndGet(1));
            liveactions.put(ac.id, ac);
            return aa;
        } catch (Exception e) {
            return null;
        }
    }

    private class ActionContainer {
        
        public ActionContainer (AdminAction aa, long id) {
            this.aa = aa;
            this.id = id;
            this.start = System.currentTimeMillis(); 
            this.end = -1;
        }
        
        public AdminAction aa;
        public long start;
        public long end; // -1 means action not executed
        public long id;
    }

    @Override
    public void run() {
        Iterator<Long> i = liveactions.keySet().iterator();
        long ts = System.currentTimeMillis(); 
        
        while (i.hasNext()) {
            long id = i.next();
            if (liveactions.get(id).end > -1 &&                //Action executed
                    ts - liveactions.get(id).end > 10*60*1000) //Action is older than 10 mins
                liveactions.remove(id);
        }
    }
}
