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

package eu.sqooss.service.web.services;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import eu.sqooss.impl.service.web.services.WebServicesImpl;
import eu.sqooss.impl.service.web.services.utils.WSPair;

/* 
 * NOTES:
 * 
 * 1. The implementation is specially here. The Axis2's wsdl generator is the reason.
 * It doesn't work correct with the interfaces and the abstract classes.
 * 
 * 2. java2wsdl doesn't support methods overloading.
 * 
 */

/**
 * Web services service exports a single interface with all required function calls.
 * The URL is: http:/.../[web.service.context]/services/[web.service.name]
 * The wsdl file is: http:/.../[web.service.context]/services/[web.service.name]?wsdl
 */
public class WebServices {
    
    private WebServicesImpl webServices;
    
    public WebServices(BundleContext bc, ServiceTracker securityTracker) {
        webServices = new WebServicesImpl(bc, securityTracker);
    }
    
    //5.1.1
    public WSPair[] evaluatedProjectsList(String userName, String password) {
        return null;
    }
    
    public WSPair[] retrieveMetrics4SelectedProject(String userName,
            String password, String projectId) {
        return null; 
    }
    
    public String retrieveSelectedMetric(String userName, String password,
            String projectId, String metricId) {
        return null;
    }
    //5.1.1
    
    //5.1.2
    public String[] retrieveFileList(String userName, String password, String projectId) {
        return null;
    }
    
    public WSPair[] retrieveMetrics4SelectedFiles(String userName, String password,
            String projectId, String[] folders, String[] fileNames) {
        return null;
    }
    //5.1.2
    
    //5.1.3
    public void requestEvaluatin4Project(String userName, String password,
            String projectName, String projectVersion,
            String srcRepositoryLocation, String srcRepositoryType,
            String mailingListLocation, String BTSLocation) {
    }
    //5.1.3
    
    //5.1.4
    public WSPair[] requestPastEvolEstimProjects(String userName, String password) {
        return null;
    }
    
    public String[] requestProjectEvolutionEstimates(String userName, String password,
            String projectId, String startDate, String endDate) {
        return null;
    }
    
    public String[] requestProjectEvolutionEstimatesDuration(String userName, String password,
            String projectId, String duration) {
        return null;
    }
    
    public String[] requestEvolEstimates4Project(String userName, String password,
            String projectName, String projectVersion, String srcRepositoryLocation,
            String srcRepositoryType, String mailingListLocation, String BTSLocation) {
        return null;
    }
    //5.1.4
    
    //5.1.5
    public WSPair[] requestProjectsWithBTS(String userName, String password) {
        return null;
    }
    
    public String[] requestDefectStatistics(String userName, String password,
            String prokectId, String searchQuery, String statisticalScheme) {
        return null;
    }
    //5.1.5
    
    //5.1.6
    public WSPair[] retrieveDevelopers4SelectedProject(String userName, String password,
            String projectId) {
        return null;
    }
    
    public WSPair[] retrieveCriteria4SelectedDeveloper(String userName, String password,
            String projectId, String developerId) {
        return null;
    }
    
    public String displayDeveloperInfoTimeDiagram(String userName, String password,
            String projectId, String developerId, String criterioId,
            String tdStart, String tdEnd) {
        return null;
    }
    
    public String displayDeveloperInfo(String userName, String password,
            String projectId, String developerId, String criterioId, String display) {
        return null;
    }
    //5.1.6
    
    //5.1.7
    public WSPair[] evaluatedProjectsListScore(String userName, String password) {
        return null;
    }
    
    public void submitScores(String userName, String password, String projectId,
            String[] scores, String textOpinion) {
        
    }
    
    public String[] viewScores(String userName, String password, String projectId) {
        return null;
    }
    
    public String[] viewComments(String userName, String password, String projectId) {
        return null;
    }
    //5.1.7
    
    //5.1.8
    public WSPair[] ratedProjectsList(String userName, String password) {
        return null;
    }
    
    public String[] retrieveProjectRatings(String userName, String password,
            String projectId) {
        return null;
    }
    //5.1.8
    
    //5.1.9
    public String subscriptionsStatus(String userName, String password) {
        return null;
    }
    
    public void modifySubscriptions(String userName, String password,
            String newProjectNotification, String newMetricPlugin,
            String projectEvalFinished, String newProjectVersion,
            String newQualityRatings, String statistics) {
    }
    //5.1.9
    
    //5.1.10
    public void submitUser(String userNameForAccess, String passwordForAccess,
            String newAccountUserName, String newAccountSurname,
            String newAccountPassword, String newAccountUserClass) {
    }
    //5.1.10
    
    //5.1.11
    public WSPair[] displayUser(String userName, String password) {
        return null;
    }
    
    public void modifyUser(String userNameForAccess, String passwordForAccess,
            String modifyAccountUserName, String modifyAccountSurname,
            String modifyAccountPassword, String modifyAccountUserClass) {
    }
    
    public void deleteUser(String userNameForAccess, String passwordForAccess, String userId) {
        
    }
    //5.1.11
    
}

//vi: ai nosi sw=4 ts=4 expandtab
