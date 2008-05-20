/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
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

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.axis2.AxisFault;

import eu.sqooss.scl.accessor.WSMetricAccessor;
import eu.sqooss.ws.client.WsStub;
import eu.sqooss.ws.client.datatypes.WSMetric;
import eu.sqooss.ws.client.datatypes.WSMetricType;
import eu.sqooss.ws.client.datatypes.WSMetricsRequest;
import eu.sqooss.ws.client.datatypes.WSMetricsResultRequest;
import eu.sqooss.ws.client.datatypes.WSResultEntry;
import eu.sqooss.ws.client.ws.GetMetricTypesByIds;
import eu.sqooss.ws.client.ws.GetMetricTypesByIdsResponse;
//import eu.sqooss.ws.client.ws.GetMetricsByProjectId;
//import eu.sqooss.ws.client.ws.GetMetricsByProjectIdResponse;
import eu.sqooss.ws.client.ws.GetMetricsByResourcesIds;
import eu.sqooss.ws.client.ws.GetMetricsByResourcesIdsResponse;
import eu.sqooss.ws.client.ws.GetMetricsResult;
import eu.sqooss.ws.client.ws.GetMetricsResultResponse;
import eu.sqooss.ws.client.ws.GetProjectEvaluatedMetrics;
import eu.sqooss.ws.client.ws.GetProjectEvaluatedMetricsResponse;

class WSMetricAccessorImpl extends WSMetricAccessor {

    private static final String METHOD_NAME_GET_PROJECT_EVALUATED_METRICS    = "getMetricsByProjectId";
    
    private static final String METHOD_NAME_GET_METRIC_TYPES_BY_IDS          = "getMetricTypesByIds";

    private static final String METHOD_NAME_GET_METRICS_BY_RESOURCES_IDS = "getMetricsByResourcesIds";

    private static final String METHOD_NAME_GET_METRICS_RESULT           = "getMetricsResult";

    private static final WSMetricType[] EMPTY_ARRAY_METRIC_TYPES = new WSMetricType[0];
    
    private Map<String, Object> parameters;
    private String userName;
    private String password;
    private WsStub wsStub;

    public WSMetricAccessorImpl(String userName, String password, String webServiceUrl) throws WSException {
        this.userName = userName;
        this.password = password;
        parameters = new Hashtable<String, Object>();
        try {
            this.wsStub = new WsStub(webServiceUrl);
        } catch (AxisFault af) {
            throw new WSException(af);
        }
    }

    /**
     * @see eu.sqooss.scl.accessor.WSMetricAccessor#getMetricsByProjectId(long)
     */
    @Override
    public WSMetric[] getProjectEvaluatedMetrics(long projectId) throws WSException {
        GetProjectEvaluatedMetricsResponse response;
        GetProjectEvaluatedMetrics params;
        if (!parameters.containsKey(METHOD_NAME_GET_PROJECT_EVALUATED_METRICS)) {
            params = new GetProjectEvaluatedMetrics();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_PROJECT_EVALUATED_METRICS, params);
        } else {
            params = (GetProjectEvaluatedMetrics) parameters.get(
                    METHOD_NAME_GET_PROJECT_EVALUATED_METRICS);
        }
        synchronized (params) {
            params.setProjectId(projectId);
            try {
                response = wsStub.getProjectEvaluatedMetrics(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSMetric[]) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSMetricAccessor#getMetricTypesByIds(long[])
     */
    @Override
    public WSMetricType[] getMetricTypesByIds(long[] metricTypesIds) throws WSException {
        if (!isValidArray(metricTypesIds)) return EMPTY_ARRAY_METRIC_TYPES;
        GetMetricTypesByIdsResponse response;
        GetMetricTypesByIds params;
        if (!parameters.containsKey(METHOD_NAME_GET_METRIC_TYPES_BY_IDS)) {
            params = new GetMetricTypesByIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_METRIC_TYPES_BY_IDS, params);
        } else {
            params = (GetMetricTypesByIds) parameters.get(
                    METHOD_NAME_GET_METRIC_TYPES_BY_IDS);
        }
        synchronized (params) {
            params.setMetricTypesIds(metricTypesIds);
            try {
                response = wsStub.getMetricTypesByIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSMetricType[]) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSMetricAccessor#getMetricsByResourcesIds(eu.sqooss.impl.service.web.services.datatypes.WSMetricsRequest)
     */
    @Override
    public WSMetric[] getMetricsByResourcesIds(WSMetricsRequest request) throws WSException {
        GetMetricsByResourcesIdsResponse response;
        GetMetricsByResourcesIds params;
        if (request.getSkipResourcesIds()) {
            //set not null array
            request.setResourcesIds(new long[] {-1});
        }
        if (!parameters.containsKey(METHOD_NAME_GET_METRICS_BY_RESOURCES_IDS)) {
            params = new GetMetricsByResourcesIds();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_METRICS_BY_RESOURCES_IDS, params);
        } else {
            params = (GetMetricsByResourcesIds) parameters.get(
                    METHOD_NAME_GET_METRICS_BY_RESOURCES_IDS);
        }
        synchronized (params) {
            params.setRequest(request);
            try {
                response = wsStub.getMetricsByResourcesIds(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSMetric[]) normaliseWSArrayResult(response.get_return());
    }

    /**
     * @see eu.sqooss.scl.accessor.WSMetricAccessor#getMetricsResult(eu.sqooss.ws.client.datatypes.WSMetricResultRequest)
     */
    @Override
    public WSResultEntry[] getMetricsResult(WSMetricsResultRequest resultRequest)
            throws WSException {
        GetMetricsResult params;
        GetMetricsResultResponse response;
        if (!parameters.containsKey(METHOD_NAME_GET_METRICS_RESULT)) {
            params = new GetMetricsResult();
            params.setPassword(password);
            params.setUserName(userName);
            parameters.put(METHOD_NAME_GET_METRICS_RESULT, params);
        } else {
            params = (GetMetricsResult) parameters.get(METHOD_NAME_GET_METRICS_RESULT);
        }
        synchronized (params) {
            params.setResultRequest(resultRequest);
            try {
                response = wsStub.getMetricsResult(params);
            } catch (RemoteException re) {
                throw new WSException(re);
            }
        }
        return (WSResultEntry[]) normaliseWSArrayResult(response.get_return());
    }

    private static boolean isValidArray(long[] arr) {
        return ((arr != null) && (arr.length > 0));
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
