/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 by Adriaan de Groot <groot@kde.org>
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

package eu.sqooss.impl.service.tds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import eu.sqooss.service.tds.MailAccessor;
import eu.sqooss.service.logging.Logger;

/**
 * This is the implementation of the simple access to mailing
 * lists; a client obtains a MailAccessor through the public
 * interfaces of the TDS for whichever access implementation is
 * in use. This implementation assumes direct access to the
 * maildir store for message access.
 */
public class MailAccessorImpl extends NamedAccessorImpl
    implements MailAccessor {
    /**
     * Where in the filesystem is the root of the message
     * folder hierarchy for the project this accessor is bound to?
     */
    private File maildirRoot;

    /**
     * Every maildir folder has three subdirectories,
     * indicating message status. These are their names.
     */
    private String[] subdirs = { "cur", "new", "tmp" };

    /**
     * Logger instance common across the TDS.
     */
    public static Logger logger = null;

    /**
     * Ten. The number of header lines to scan while looking
     * for a Date: line in mail messages.
     */
    private static final int TEN_LINES = 10;

    /**
     * Five. The length of the string 'Date: '.
     */
    private static final int FIVE_CHARS = 5;

    /**
     * Create a mail accessor for the given project. The
     * project @p id is definitive, while the @p name is
     * informational in human-readable text only. The
     * root of the mailing list hierarcht for the project
     * is given by @p root, which should be a directory.
     */
    public MailAccessorImpl( final long id, final String name,
        final File root ) {
        super(id,name);
        maildirRoot = root;
    }

    /**
     * Read a file @p f and return its contents as a single String,
     * possibly preserving newlines (I'm not sure what readLine() does)
     * assuming the file is a text file and encoded in the default encoding.
     */
    private String readFile( File f )
        throws FileNotFoundException {
        BufferedReader in = new BufferedReader(new FileReader(f));
        StringBuilder s = new StringBuilder();
        String line;

        try {
            while ( (line=in.readLine()) != null ) {
                s.append(line);
            }
        } catch (IOException e) {
            // Repurpose, pretend it was not found
            throw new FileNotFoundException(e.getMessage());
        }

        return s.toString();
    }

    /**
     * Return a File object for the given @p listId or throw FileNotFoundException
     * if the list does not exist.
     */
    private File getFolder( String listId )
        throws FileNotFoundException {
        File listDir = new File(maildirRoot, listId);
        if (!listDir.exists() || !listDir.isDirectory()) {
            throw new FileNotFoundException("ListID <" + listId + "> does not exist.");
        }
        return listDir;
    }

    /**
     * Return a file object for the given @p messageId within the mailing
     * list folder @p listDir . This searches the normal maildir subfolders
     * new, cur and tmp (tmp is a bad idea, actually). Throws FileNotFoundException
     * if the message does not exist in any of the maildir subdirectories.
     */
    private File getMessageFile( File listDir, String messageId )
        throws FileNotFoundException {
        for(String s : subdirs) {
            File msgFile = new File(listDir, s + File.separator + messageId);
            if (msgFile.exists()) {
                return msgFile;
            }
        }
        throw new FileNotFoundException("Message <" + messageId + "> does not exist.");
    }

    /**
     * Read the first @p limit lines of maildir file @p msgFile looking
     * for a line that starts with @p hdr (ie. look for the header
     * near the beginning of the message). Blank lines, which signal
     * the end of headers, cause the scan to end regardless of the
     * value of @p limit.
     *
     * May return null if the header is not found.
     */
    private String scanForHeader( File msgFile, String hdr, int limit )
        throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(msgFile));
        for (int i=0; i<limit; ++i) {
            String line = in.readLine();
            if (line.startsWith(hdr)) {
                return line;
            }
            // Blank line signals end of headers
            if (line.length() < 1) {
                break;
            }
        }
        return null;
    }

    // Interface methods
    public String getRawMessage(String listId, String id)
        throws FileNotFoundException {
        File listDir = getFolder(listId);
        for (String s : subdirs) {
            File msgFile = new File(listDir, s + File.separator + id);
            if (msgFile.exists()) {
                return readFile(msgFile);
            }
        }
        throw new FileNotFoundException("No message <" + id + ">");
    }

    public List < String > getMessages(String listId)
        throws FileNotFoundException {
        File listDir = getFolder(listId);
        List < String > l = new LinkedList < String >();

        for(String s : subdirs) {
            File msgFile = new File(listDir, s);
            if (msgFile.exists() && msgFile.isDirectory()) {
                String[] entries = msgFile.list();
                for (String e : entries) {
                    l.add(e);
                }
            }
        }

        return l;
    }

    public List<String> getMessages( String listId, Date d1, Date d2 )
        throws FileNotFoundException {
        File listDir = getFolder(listId);
        List<String> allMessages = getMessages(listId);
        List<String> goodMessages = new LinkedList<String>();
        DateFormat dateParser = DateFormat.getInstance();
        for (String m : allMessages) {
            String dateHdr = null;
            try {
                File msgFile = getMessageFile(listDir,m);
                dateHdr = scanForHeader(msgFile, "Date:", TEN_LINES);
                if (dateHdr != null) {
                    Date d = dateParser.parse(dateHdr.substring(FIVE_CHARS));
                    // Check if it's in the interval [d1,d2)
                    if (!(d.before(d1) || !d.before(d2))) {
                        goodMessages.add(m);
                    }
                }
            } catch (FileNotFoundException e) {
                // Message disappeared out from under us, ignore
                // and assume message is bad
                logger.info("Message <" + m + "> vanished.");
            } catch (IOException e) {
                // scanForHeader failed; ignore and assume message is bad
                logger.info("Could not read message <" + m + ">");
            } catch (java.text.ParseException e) {
                // Bad date in maildir message, assume message is bad
                if (logger != null) {
                    logger.info("Failed to parse <"
                        + dateHdr.substring(FIVE_CHARS) + ">");
                }
            }
        }
        return goodMessages;
    }

    public final String getSender(final String listId,
        final  String id)
        throws FileNotFoundException {
        File listDir = getFolder(listId);
        File msgFile = getMessageFile(listDir, id);
        try {
            // From: ought to be very first header
            String from = scanForHeader(msgFile, "From:", 2);
            if (from != null) {
                return from;
            }
        } catch (IOException e) {
            throw new FileNotFoundException(
                "Message <" + id + "> cannot be read.");
        }

        // This is some kind of lousy joke.
        throw new FileNotFoundException(
            "Message <" + id + "> implements wrong mbox headers.");
    }
}

// vi: ai nosi sw=4 ts=4 expandtab

