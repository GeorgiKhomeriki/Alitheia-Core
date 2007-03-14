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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;


import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.admin.SVNWCAccess;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.*;


/**
 * @author circular
 * 
 * Implements the functionality required to access a SVN repository
 * 
 */
public class SvnRepository extends Repository implements ISVNLogEntryHandler {

    private SVNRepository repository;
    private ISVNAuthenticationManager authManager;
    private static SVNClientManager clientManager;
    private SVNURL url;
    private CommitLog svnCommitLog;
    //private ISVNEventHandler wcEventHandler;

    public SvnRepository(String localPath, String serverPath, String username,
            String passwd)throws InvalidRepositoryException {
        super(localPath, serverPath, username, passwd);
        
        if (this.serverPath.indexOf("svn://") != -1) {
        	/* usage over http */
        	this.serverPath = this.serverPath.replaceAll("svn://", "http://");
        } else if (this.serverPath.indexOf("svns://") != -1) {
        	/* usage over https */
        	this.serverPath = this.serverPath.replaceAll("svns://", "https://");
        } else if (this.serverPath.indexOf("svn+fsfs://") != -1) {
        	/* usage over file */
        	this.serverPath = this.serverPath.replaceAll("svn+fsfs://", "fsfs://");
        }
        
        URI uri;
        try {
            uri = new URI(this.serverPath);
        } catch (URISyntaxException e) {
            throw new InvalidRepositoryException(e.getMessage());
        }
        // check if there is a port given
        if (uri.getPort() == -1) {
        	this.serverPath = uri.getScheme() + uri.getHost() + uri.getPath();
        } else {
            String port = new Integer(uri.getPort()).toString();
            this.serverPath = uri.getScheme() + uri.getHost() + uri.getPath() + ":" + port;
        }
        
        initializeFactories();
        repository = null;
    }

    @Override
    public void checkout() {
    	initializeRepository();
    	SVNUpdateClient updater = new SVNUpdateClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	try {
			updater.doCheckout(url, new File(localPath),
					SVNRevision.HEAD, 
					SVNRevision.HEAD /* denotes the latest repository revision */, 
					true /* when true, checkout descends recursively */);
		} catch (SVNException svne) {
			System.err.println("Couldn't checkout");
			svne.printStackTrace();
		}
    }

    @Override
    public void checkout(Revision rev) {
    	initializeRepository();
    	SVNUpdateClient updater = new SVNUpdateClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	/* SVNRevision is a revision wrapper used for an abstract 
    	 * representation of revision information */
    	SVNRevision tmpRev = SVNRevision.create(rev.getNumber());
    	try {
			updater.doCheckout(url, new File(localPath), tmpRev, tmpRev, true);
		} catch (SVNException svne) {
			System.err.println("Couldn't checkout");
			svne.printStackTrace();
		}
    }

    @Override
    public void update(Revision rev) {
    	initializeRepository();
    	SVNUpdateClient updater = new SVNUpdateClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	SVNRevision tmpRev = SVNRevision.create(rev.getNumber());
    	try {
			updater.doUpdate(new File(localPath), tmpRev, true);
		} catch (SVNException svne) {
			System.err.println("Couldn't update");
			svne.printStackTrace();
		}
    }

    @Override
    public Diff diff(Revision rev) {
    	SVNDiffClient diffClient = new SVNDiffClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	SVNRevision tmpRev = SVNRevision.create(rev.getNumber());
        return null;
    }

    @Override
    public Diff diff(Revision start, Revision end) {
    	SVNDiffClient diffClient = new SVNDiffClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	SVNRevision tmpRevStart = SVNRevision.create(start.getNumber());
    	SVNRevision tmpRevEnd = SVNRevision.create(end.getNumber());
        return null;
    }

    @Override
    public CommitLog getLog(Revision start, Revision end) {
    	initializeRepository();
    	SVNLogClient logger = new SVNLogClient(authManager, 
    			SVNWCUtil.createDefaultOptions(true));
    	SVNRevision tmpRevStart = SVNRevision.create(start.getNumber());
    	SVNRevision tmpRevEnd = SVNRevision.create(end.getNumber());
    	ISVNLogEntryHandler handler = this;
    	svnCommitLog = new CommitLog(start, end);
    	svnCommitLog.clear();
    	try {
			logger.doLog(url, null, tmpRevStart, tmpRevStart, tmpRevEnd, 
					false, /* copies history will be also included into processing */
					true /* report all changed paths for every revision being processed */,
					Long.MAX_VALUE, /* maximum number of log entries to be processed */
					handler /* Interface ISVNLogEntryHandler implemented below */);
		} catch (SVNException svne) {
			System.err.println("Couldn't doLog");
			svne.printStackTrace();
		}
        return svnCommitLog;
    }

    @Override
    public String getCurrentVersion(boolean remote) {
        initializeRepository();
        if(remote) {          
            try {
                revision = new Revision(repository.getLatestRevision());
            } catch (SVNException svne) {
                revision = new Revision(-1);
            }
        } else {
            clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), authManager);
            try {
                SVNInfo info = clientManager.getWCClient().doInfo(
                        new File(localPath), SVNRevision.WORKING);  
                revision = new Revision(info.getRevision().getNumber());        
            } catch (SVNException svne) {
                System.err.println("Error while retrieving info for the "
                        + "working copy of '" + serverPath + "' at "
                        + localPath + ": " + svne.getMessage());
            }
        }
        return revision.getDescription();
    }

    /**
     * Initializes the SVNKit library to work with dirrerent repository
     * remote access methods
     */
    private static void initializeFactories() {

        // for using over http:// and https://
        DAVRepositoryFactory.setup();

        //for using over svn:// and svn+xxx://
        SVNRepositoryFactoryImpl.setup();

        //for using over file://
        FSRepositoryFactory.setup();
    }

    private void initializeRepository() {
        if(repository != null) {
            return;
        }
        
        try {
            //SVNURL url = SVNURL.parseURIDecoded(serverPath);
        	url = SVNURL.parseURIDecoded(serverPath);
            repository = SVNRepositoryFactoryImpl.create(url);

            /* 
             * Default authentication manager first attempts to use provided
             * user name and password and then falls back to the credentials
             * stored in the default Subversion credentials storage that is 
             * located in Subversion configuration area. We dont need / like
             * this kind of behaviour, so we use BasicAuthenticationManager.
             * If anonymous access is requested the authentication is skipped.
             */
            if ((username.length() > 0) && (password.length() > 0)) {
                authManager = new 
                    BasicAuthenticationManager(username, password);

                repository.setAuthenticationManager(authManager);
            }
        } catch (SVNException svne){
            //Probably a malformed URL was provided
            System.err.println("Error while creating an SVNRepository for '"
                    + serverPath + "': " + svne.getMessage());
        }
    }
    
    /** 
     * handleLogEntry handles a log entry passed. Here we use it 
     * to add CommitLogEntr-ies to a CommitLog object
     */
    public void handleLogEntry(SVNLogEntry logEntry) {
    	if (logEntry == null || (logEntry.getMessage() == null && logEntry.getRevision() == 0)) {
            return;
        }
    	Long tmpRev = logEntry.getRevision();
    	CommitLogEntry tmpEntry = new CommitLogEntry(logEntry.getAuthor(), 
    			logEntry.getMessage(), 
    			logEntry.getDate(), 
    			tmpRev.toString());
    	svnCommitLog.add(tmpEntry);
    }
}
