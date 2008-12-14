/*
This file is part of the Alitheia system, developed by the SQO-OSS
consortium as part of the IST FP6 SQO-OSS project, number 033331.

Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
Copyright 2007-2008 by KDAB (www.kdab.net)
Author: Mirko Boehm <mirko@kdab.net>

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

package eu.sqooss.service.scheduler;

import java.util.Set;

/**
 * Interface for the scheduler.
 *
 * @author Mirko Boehm
 */
public interface Scheduler {

    /**
     * Queue a job.
     * 
     * @param job - the job.
     */
    void enqueue(Job job) throws SchedulerException;

    /**
     * Queue 
     */
    void enqueueNoDependencies(Set<Job> jobs) throws SchedulerException;
    
    /**
     * This method is called, when the state of the job \a job changes to 
     * \a state.
     */
    void jobStateChanged(Job job, Job.State state);

    /**
     * This method is called, when dependencies of the job \a were changed.
     */
    void jobDependenciesChanged(Job job);

    /**
     * Returns a job which can be executed.
     * If there's currently no job available, this method is blocking.
     * @return The job.
     */
    Job takeJob() throws java.lang.InterruptedException;
    
    /**
     * Tries to take a specific job from the job queue.
     * Unlike takeJob(), this method is not blocking.
     * @param job The job wanted.
     * @return The job.
     * @throws SchedulerException When the job isn't enqueued in the Scheduler
     * or it is already running.
     */
    Job takeJob(Job job) throws SchedulerException;

    /**
     * Starts job execution using \a n additional worker threads.
     * \a n new worker threads are created. Even if the scheduler is already
     * running some.
     */
    void startExecute(int n);

    /**
     * Stops job execution of all current worker threads.
     */
    void stopExecute();
    
    /**
     * @return Whether the scheduler is currently executing jobs or not.
     */
    boolean isExecuting();
    
    /**
     * Get statistics
     * @return A copy of the current statistics object
     */
    SchedulerStats getSchedulerStats();
    
    /**
     * Get a copy of the failed job queue
     */
    Job[] getFailedQueue();
    
    /**
     * Get the list of threads working on jobs of this scheduler.
     */
    WorkerThread[] getWorkerThreads();
    
    /**
     * Starts a temporary worker thread handling exactly one job.
     */
    void startOneShotWorkerThread();
}
