/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 Georgios Gousios <gousiosg@aueb.gr>
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import eu.sqooss.core.AlitheiaCore;

import eu.sqooss.service.db.DAOException;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.MailingList;
import eu.sqooss.service.db.Sender;
import eu.sqooss.service.db.StoredProject;

import eu.sqooss.service.logging.Logger;

import eu.sqooss.service.scheduler.Job;

import eu.sqooss.service.tds.MailAccessor;
import eu.sqooss.service.tds.TDAccessor;

import eu.sqooss.service.updater.UpdaterException;

/**
 * Synchronises raw mails with the database
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
class MailUpdater extends Job {
    private StoredProject project;
    private AlitheiaCore core;
    private Logger logger;

    public MailUpdater(StoredProject project,
                       AlitheiaCore core,
                       Logger logger) throws UpdaterException {
        if (project == null || core == null || logger == null) {
            throw new UpdaterException("Cannot initialise MailUpdater (path/core/logger is null)");
        }

        this.core = core;
        this.project = project;
        this.logger = logger;
    }

    public int priority() {
        return 0;
    }

    protected void run() {
        try {
            TDAccessor spAccessor = core.getTDSService().getAccessor(project.getId());
            MailAccessor mailAccessor = spAccessor.getMailAccessor();
            List<MailingList> mllist = MailingList.getListsPerProject(project);
            for ( MailingList ml : mllist ) {
                processList(mailAccessor, ml);
            }
        } catch ( DAOException daoe ) {
            logger.warn(daoe.getMessage());
        }
    }

    private void processList(MailAccessor mailAccessor, MailingList mllist) {
        List<String> messageIds = null;
        String listId = mllist.getListId();
        DBService dbs = core.getDBService();
        try {
            messageIds = mailAccessor.getMessages(listId);
        } catch (FileNotFoundException e) {
            logger.warn("Mailing list <" + listId + "> vanished: " + e.getMessage());
            return;
        }

        // TODO: not the best way to do it, but it works
        // still needs to do it with Dates
        for ( String messageId : messageIds ) {
            String msg = String.format("Message <%s> in list <%s> ", messageId, listId);

            logger.info(msg);
            try {
                MimeMessage mm = mailAccessor.getMimeMessage(listId, messageId);
                if (mm == null) {
                	logger.info("Failed to parse message.");
                	continue;
                }
                logger.info("Message has " + mm.getLineCount() + " lines of content.");
                Enumeration<String> headerStrings = mm.getAllHeaderLines();
                
                while (headerStrings.hasMoreElements()) {
                	logger.info("Header line <" + headerStrings.nextElement() + ">");
                }
                Address[] senderAddr = mm.getFrom();
                if (senderAddr == null) {
                	logger.info("Message has no sender?");
                	continue;
                }
                for (Address a : senderAddr) {
                	logger.info("Found sender <" + a + ">");
                }
                Sender sender = Sender.getSenderByEmail(senderAddr[0].toString());
                if (sender == null) {
                    sender = new Sender(senderAddr.toString());
                    dbs.addRecord(sender);
                }
                MailMessage mmsg = MailMessage.getMessageById(messageId);
                if (mmsg == null) { mmsg = new MailMessage();
                    mmsg.setListId(mllist);
                    mmsg.setMessageId(mm.getMessageID());
                    mmsg.setSender(sender);
                    mmsg.setSendDate(mm.getSentDate());
                    mmsg.setArrivalDate(mm.getReceivedDate());
                    mmsg.setSubject(mm.getSubject());
                    dbs.addRecord(mmsg);
                }
			} catch (FileNotFoundException e) {
				logger.warn(msg + "not found: " + e.getMessage());
			} catch (MessagingException me) {
				logger.warn(msg + " could not be parsed! - " + me.toString());
			} catch (DAOException daoe) {
				logger.warn(msg + " error - " + daoe.toString());
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn(msg + " error - " + e.getMessage());
			}
		}
	}

}


// vi: ai nosi sw=4 ts=4 expandtab

