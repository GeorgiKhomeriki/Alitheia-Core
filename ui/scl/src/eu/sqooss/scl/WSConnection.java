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

package eu.sqooss.scl;

import eu.sqooss.scl.result.WSResult;

public interface WSConnection {
    
    //5.1.1
    public WSResult evaluatedProjectsList() throws WSException;
    
    public WSResult retrieveMetrics4SelectedProject(String projectId) throws WSException;
    
    public WSResult retrieveSelectedMetric(String projectId, String metricId) throws WSException;
    //5.1.1
    
    //5.1.2
    public WSResult retrieveFileList(String projectId);
    
    public WSResult retrieveMetrics4SelectedFiles(String projectId, String[] folders, String[] fileNames);
    //5.1.2
    
    //5.1.3
    public void requestEvaluatin4Project(String projectName, String projectVersion,
            String srcRepositoryLocation, String srcRepositoryType,
            String mailingListLocation, String BTSLocation);
    //5.1.3
    
    //5.1.4
    public WSResult requestPastEvolEstimProjects();
    
    public WSResult requestProjectEvolutionEstimates(String projectId, String startDate, String endDate);
    
    public WSResult requestProjectEvolutionEstimatesDuration(String projectId, String duration);
    
    public WSResult requestEvolEstimates4Project(String projectName, String projectVersion, String srcRepositoryLocation,
            String srcRepositoryType, String mailingListLocation, String BTSLocation);
    //5.1.4
    
    //5.1.5
    public WSResult requestProjectsWithBTS();
    
    public WSResult requestDefectStatistics(String prokectId, String searchQuery, String statisticalScheme);
    //5.1.5
    
    //5.1.6
    public WSResult retrieveDevelopers4SelectedProject(String projectId);
    
    public WSResult retrieveCriteria4SelectedDeveloper(String projectId, String developerId);
    
    public WSResult displayDeveloperInfoTimeDiagram(String projectId, String developerId, String criterioId,
            String tdStart, String tdEnd);
    
    public WSResult displayDeveloperInfo(String projectId, String developerId, String criterioId, String display);
    //5.1.6
    
    //5.1.7
    public WSResult evaluatedProjectsListScore();
    
    public void submitScores(String projectId, String[] scores, String textOpinion);
    
    public WSResult viewScores(String projectId);
    
    public WSResult viewComments(String projectId);
    //5.1.7
    
    //5.1.8
    public WSResult ratedProjectsList();
    
    public WSResult retrieveProjectRatings(String projectId);
    //5.1.8
    
    //5.1.9
    public WSResult subscriptionsStatus();
    
    public void modifySubscriptions(String newProjectNotification, String newMetricPlugin,
            String projectEvalFinished, String newProjectVersion,
            String newQualityRatings, String statistics);
    //5.1.9
    
    //5.1.10
    public void submitUser(String newAccountUserName, String newAccountSurname,
            String newAccountPassword, String newAccountUserClass);
    //5.1.10
    
    //5.1.11
    public WSResult displayUser();
    
    public void modifyUser(String modifyAccountUserName, String modifyAccountSurname,
            String modifyAccountPassword, String modifyAccountUserClass);
    
    public void deleteUser(String userId);
    //5.1.11
    
}

//vi: ai nosi sw=4 ts=4 expandtab
