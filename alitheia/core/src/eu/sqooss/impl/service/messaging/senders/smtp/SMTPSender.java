/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
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

package eu.sqooss.impl.service.messaging.senders.smtp;

import java.util.Properties;
import java.util.Vector;

import org.osgi.framework.BundleContext;

import eu.sqooss.impl.service.messaging.senders.smtp.connection.Constants;
import eu.sqooss.impl.service.messaging.senders.smtp.connection.DefaultSASLFactory;
import eu.sqooss.impl.service.messaging.timer.Timer;
import eu.sqooss.service.messaging.Message;
import eu.sqooss.service.messaging.sender.MessageSender;

/**
 * The default sender of the messages is <code>SMTPSender</code>.
 * It is registered as service with property <code>Message.PROTOCOL_PROPERTY</code> and
 * property value <code>SMTPSender.PROTOCOL_PROPERTY_VALUE</code>.
 */
public class SMTPSender implements MessageSender {

    public static final String PROTOCOL_PROPERTY_VALUE = "smtp";

    private static final String SMTP_TIMER_NAME = "SMTP Timer ";
    private static final long DEFAULT_SESSION_TIMEOUT = 2*60*1000;

    private Object sessionsLockObject = new Object();
    private Object propertiesLockObject = new Object();
    private Timer timer;
    private Vector < SMTPSession > sessions;
    private BundleContext bc;
    private boolean isStopped;

    //session properties
    private String sessionPort;
    private String sessionHost;
    private String sessionUser;
    private String sessionPass;
    private long sessionTimeout;
    private String reply;

    public SMTPSender(BundleContext bc) {
        this.bc = bc;
        this.sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        sessions = new Vector < SMTPSession >();
        isStopped = false;
        timer = new Timer(SMTP_TIMER_NAME);
        timer.start();
    }

    /**
     * @see eu.sqooss.service.messaging.sender.MessageSender#sendMessage(eu.sqooss.service.messaging.Message)
     */
    public int sendMessage(Message message) {
        if ((reply == null) || (reply.trim().equals(""))) {
            throw new IllegalArgumentException("The message's reply is not set!");
        }
        Properties sessionProperties = getSessionProperties();
        DefaultSASLFactory saslFactory = new DefaultSASLFactory();
        SMTPSession session = new SMTPSession(bc, sessionProperties, sessionTimeout, timer);
        synchronized (sessionsLockObject) {
            sessions.addElement(session);
        }
        session.setSASLFactory(saslFactory);
        try {
            if (isStopped) {
                return Message.STATUS_FAILED;
            }
            session.open();
            StringBuffer messageBody = new StringBuffer();
            //adds subject
            messageBody.append("Subject: " + message.getTitle() + "\r\n");
            //adds from
            messageBody.append("From: " + reply + "\r\n");
            //adds to
            messageBody.append("To: ");
            Vector<String> recipients = message.getRecipients();
            for (int i = 0; i < recipients.size(); i++) {
                messageBody.append((String)recipients.elementAt(i));
                messageBody.append(", ");
            }
            messageBody.append("\r\n");
            //adds text
            messageBody.append(message.getBody());
            session.send(message.getRecipients(), reply, messageBody.toString());
            session.close();
            synchronized (sessionsLockObject) {
                sessions.remove(session);
            }
        } catch (Exception e) {
            synchronized (sessionsLockObject) {
                session.timedOut();
                sessions.remove(session);
            }
            return Message.STATUS_FAILED;
        }
        return Message.STATUS_SENT;
    }

    /**
     * Stops the sender.
     */
    public void stopService() {
        if (!isStopped) {
            timer.stop();
            synchronized (sessionsLockObject) {
                SMTPSession session;
                for (int i = 0; i < sessions.size(); i++) {
                    session = (SMTPSession)sessions.elementAt(i);
                    session.timedOut();
                }
                isStopped = true;
            }
        }
    }

    /**
     * Sets the port.
     * @param sessionPort the new port
     */
    public void setSessionPort(String sessionPort) {
        synchronized (propertiesLockObject) {
            this.sessionPort = sessionPort;
        }
    }

    /**
     * Sets the host.
     * @param sessionHost the new host
     */
    public void setSessionHost(String sessionHost) {
        synchronized (propertiesLockObject) {
            this.sessionHost = sessionHost;
        }
    }

    /**
     * Sets the user.
     * @param sessionUser the new user
     */
    public void setSessionUser(String sessionUser) {
        synchronized (propertiesLockObject) {
            this.sessionUser = sessionUser;
        }
    }

    /**
     * Sets the password.
     * @param sessionPass the new password
     */
    public void setSessionPass(String sessionPass) {
        synchronized (propertiesLockObject) {
            this.sessionPass = sessionPass;
        }
    }

    /**
     * Sets the timeout of the session.
     * @param sessionTimeout the new timeout
     */
    public void setSessionTimeout(long sessionTimeout) {
        if (sessionTimeout < 0) {
            this.sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        } else {
            this.sessionTimeout = sessionTimeout;
        }
    }

    /**
     * Sets the reply address.
     * @param reply the new reply address
     */
    public void setReply(String reply) {
        this.reply = reply;
    }

    private Properties getSessionProperties() {
        synchronized (propertiesLockObject) {
            Properties sessionProperties = new Properties();
            if ((sessionHost == null) || (sessionHost.trim().equals(""))) {
                sessionProperties.put(Constants.HOST, "localhost");
            } else {
                sessionProperties.put(Constants.HOST, sessionHost);
            }

            if ((sessionPort != null) && (!sessionPort.trim().equals(""))) {
                try {
                    Integer.parseInt(sessionPort);
                    sessionProperties.put(Constants.PORT, sessionPort);
                } catch (NumberFormatException nfe) {
                    //default smtp port is 25
                }
            }

            if ((sessionUser != null) && (!sessionUser.trim().equals(""))) {
                sessionProperties.put(Constants.USER, sessionUser);
            }

            if ((sessionPass != null) && (!sessionPass.trim().equals(""))) {
                sessionProperties.put(Constants.PASS, sessionPass);
            }

            return sessionProperties;
        }
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
