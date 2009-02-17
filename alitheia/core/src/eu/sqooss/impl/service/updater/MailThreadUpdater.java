    /*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2009 - Organization for Free and Open Source Software,  
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

package eu.sqooss.impl.service.updater;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.internet.MimeMessage;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.MailingList;
import eu.sqooss.service.db.MailingListThread;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.tds.InvalidAccessorException;
import eu.sqooss.service.tds.MailAccessor;
import eu.sqooss.service.updater.UpdaterException;

/**
 * Job that organises emails in threads. Should be started each time a 
 * mailing list has received new emails.
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 *
 */
public class MailThreadUpdater extends Job {

    private MailingList ml;
    private String projectName;
    private Logger logger;
    private DBService dbs;
    private MailAccessor mailAccessor;
    
    private Set<Long> updMailThreads = new TreeSet<Long>(); 
    
    public MailThreadUpdater(MailingList ml, Logger l)
            throws UpdaterException {
        this.ml = ml;
        this.logger = l;
        this.projectName = ml.getStoredProject().getName();
        dbs = AlitheiaCore.getInstance().getDBService();
        try {
            mailAccessor = AlitheiaCore.getInstance().getTDSService().getAccessor(
                    ml.getStoredProject().getId()).getMailAccessor();
        } catch (InvalidAccessorException e) {
            err("Could not get MailAccessor for project" 
                    + ml.getStoredProject().getName());
        }
    }

    public int priority() {
        return 0x2;
    }

    @Override
    protected void run() throws Exception {
        dbs.startDBSession();
        ml = dbs.attachObjectToDBSession(ml);
        int newThreads = 0, updatedThreads = 0;
        MailMessage lastEmail = null;
        lastEmail = ml.getLatestEmail();
        HashMap<String, MimeMessage> processed = new HashMap<String, MimeMessage>();
        
        if (lastEmail == null) {
            info("No mail messages for list " + ml);
            dbs.commitDBSession();
            return; //No messages for this mailing list
        }
        
        String paramMl = "paramMl";
        String query = " select mm.id " +
            " from  MailingList ml, MailMessage mm" +
            " where mm.list = ml " +
            " and ml = :" + paramMl +
            " and mm.thread is null " +
            " order by mm.sendDate asc"; 
            
        Map<String,Object> params = new HashMap<String, Object>(1);
        params.put(paramMl, ml);
        
        List<Long> mmList = (List<Long>) dbs.doHQL(query, params);
        
        if (mmList.isEmpty()) {
            info("No unprocessed mail messages found for list " + ml);
            dbs.commitDBSession();
            return;
        }
        
        for (Long mailId : mmList) {
            if (!dbs.isDBSessionActive())
                dbs.startDBSession();
            MailMessage mail = MailMessage.loadDAObyId(mailId, MailMessage.class);
            
            // Message has been already added to thread
            if (mail.getThread() != null)
                continue;

            MimeMessage mm = mailAccessor.getMimeMessage(ml.getListId(), 
                    mail.getFilename());
            
            processed.put(mail.getFilename(), mm);

            /* Thread identification code. Naive, but works */
            String[] inReplyTo = mm.getHeader("In-Reply-To");
            String[] references = mm.getHeader("References");
            boolean newThread = false, reference = false;
            String parentId = null;

            if (inReplyTo == null) {
                if (references == null) {
                    newThread = true;
                } else {
                    // Arbitrarily set first message reference as parent.
                    // The mime message protocol does not specify any such
                    // ordering, in fact it does not specify any ordering
                    // scheme at all.
                    parentId = references[0];
                    reference = true;
                }
            } else {
                /*
                 * In most cases, the first in-reply-to entry corresponds to the
                 * answered email. If not, the thread is still valid.
                 */
                parentId = inReplyTo[0];
            }

            MailingListThread mlt = null;
            /* Get the parent mail object */
            MailMessage parentMail = MailMessage.getMessageById(parentId);

            if (parentId != null) { 
                /* Parent-less child, parent might have arrived later */
                if (parentMail == null || parentMail.getThread() == null) {
                    newThread = true;
                } else {
                    /*
                     * Mails identified as children to a thread only by the
                     * References header, are placed at the same depth level as
                     * their parent (Usenet news style).
                     */
                    int depth = parentMail.getDepth();
                    if (!reference) {
                        depth = depth + 1;
                    }
                    /* Add the processed message as child to the parent's thread */
                    mail.setDepth(depth);
                    mail.setParent(parentMail);
                    mail.setThread(parentMail.getThread());
                    parentMail.getThread().setLastUpdated(mail.getSendDate());
                    debug("Updating thread " + parentMail.getThread().getId());
                    updMailThreads.add(parentMail.getThread().getId());
                    updatedThreads++;
                }
            }

            if (newThread) {
                boolean childExists = false;

                /*
                 * Check if a child mail has arrived before the processed mail.
                 */
                for (String key : processed.keySet()) {
                    MimeMessage child = processed.get(key);
                    if ((child.getHeader("In-Reply-To") != null && 
                            child.getHeader("In-Reply-To")[0].equals(mail.getMessageId()))
                       || (child.getHeader("References") != null && 
                            child.getHeader("References")[0].equals(mail.getMessageId()))) {

                        childExists = true;

                        /* Get message whose parent is the discovered child */
                        MailMessage childMM = MailMessage.getMessageById(child.getMessageID());
                        
                        if (childMM == null) {
                            warn("Supposedly processed child of message "
                                    + mail + " not found in DB.");
                            childExists = false;
                            break;
                        }
                        
                        MailingListThread thr = childMM.getThread();

                        /*
                         * Set the old thread parent as child of the current
                         * mail and current email as top level parent of the
                         * thread 
                         */
                        childMM.setParent(mail);
                        mail.setParent(null);
                        mail.setDepth(0);
                        childMM.getThread().setLastUpdated(mail.getSendDate());
                        debug("Reconstructing thread " + thr.getId());

                        /* New top level email added, increase depth level in children messages */
                        for (MailMessage msg : thr.getMessages()) {
                            msg.setDepth(msg.getDepth() + 1);
                        }
                        updMailThreads.add(thr.getId());
                        updatedThreads++;
                    }
                }

                if (childExists)
                    continue;

                /* Create a new thread */
                mlt = new MailingListThread(ml, mail.getSendDate());
                dbs.addRecord(mlt);
                mail.setThread(mlt);
                mail.setDepth(0);
                debug("Adding new thread " + mlt.getId());
                updMailThreads.add(mlt.getId());
                newThreads++;
            }
            if (mail.getThread() == null) 
                warn("Mail message " + mail + " was not assigned any thread");
            
            dbs.commitDBSession();
        }
        dbs.startDBSession();
        info("Mail thread updater - " + ml 
                + " " + newThreads + " new threads, " + updatedThreads 
                + " thread updates" );
        AlitheiaCore.getInstance().getMetricActivator().runMetrics(updMailThreads, MailingListThread.class);
        if (dbs.isDBSessionActive()) dbs.commitDBSession();
    }   
    
    @Override
    public String toString() {

        return "MailThreadUpdater Job - Project:{" + projectName 
        + "}, Mailing List: {" + ml.getListId() + "}";
        
    }
    
    private void warn(String message) {
        logger.warn(projectName + ":" + message);
    }
    
    private void err(String message) {
        logger.error(projectName + ":" + message);
    }
    
    private void info(String message) {
        logger.info(projectName + ":" + message);
    }
    
    private void debug(String message) {
        logger.debug(projectName + ":" + message);
    }
}
