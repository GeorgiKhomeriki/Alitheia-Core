/*
This file is part of the Alitheia system, developed by the SQO-OSS
consortium as part of the IST FP6 SQO-OSS project, number 033331.

Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
Copyright 2007-2008 by KDAB (www.kdab.net)
Author: Christoph Schleifenbaum <christoph@kdab.net>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package eu.sqooss.impl.service.scheduler;

import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.scheduler.Job;

import java.lang.InterruptedException;

/**
 * Worker thread executing jobs given by a scheduler.
 *
 * @author Christoph Schleifenbaum
 */
class WorkerThread extends Thread
{

    private Scheduler m_scheduler;

    private volatile boolean m_processing;

    /**
     * Constructor creating a new WorkerThread
     * @param s the schedule being asked for jobs.
     */
    public WorkerThread( Scheduler s ) {
        m_scheduler = s;
    }

    /**
     * Runs the worker thread.
     */
    public void run()
    {
        m_processing = true;
        while (m_processing) {
            Job j = null;
            try {
                // get a job from the scheduler
                j = m_scheduler.takeJob();
            } catch (InterruptedException e) {
                // we were inerrupted, just try again
                continue;
            }

            try {
                // execute the job
                j.execute();
            } catch( Exception e ) {
                // TODO: Error handling?
            }
        }
    }

    /**
     * Stops processing of jobs, after the current job has finished.
     */
    public void stopProcessing() {
        m_processing = false;
        interrupt();
    }
}
