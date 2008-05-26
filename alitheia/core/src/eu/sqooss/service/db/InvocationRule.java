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

package eu.sqooss.service.db;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import eu.sqooss.service.db.MetricType.Type;

public class InvocationRule extends DAObject {
    private Long prevRule = null;
    private Long nextRule = null;
    private String scope = null;
    private String value = null;
    private String action = null;
    private StoredProject project = null;
    private Plugin plugin = null;
    private MetricType metricType = null;

    public enum ActionType {
        EVAL,
        SKIP;

        public static ActionType fromString(String action) {
            if (action.equals(EVAL.toString()))
                return EVAL;
            else if (action.equals(SKIP.toString()))
                return SKIP;
            else
                return null;
        }
    };

    public enum ScopeType {
        ALL,
        EXACT,
        EACH,
        FROM,
        TO,
        RANGE,
        LIST;

        public static ScopeType fromString(String scope) {
            if (scope.equals(ALL.toString()))
                return ALL;
            if (scope.equals(EXACT.toString()))
                return EXACT;
            else if (scope.equals(EACH.toString()))
                return EACH;
            else if (scope.equals(FROM.toString()))
                return FROM;
            else if (scope.equals(TO.toString()))
                return TO;
            else if (scope.equals(RANGE.toString()))
                return RANGE;
            else if (scope.equals(LIST.toString()))
                return LIST;
            else
                return null;
        }
    };

    public Long getPrevRule() {
        return prevRule;
    }

    public void setPrevRule(Long ruleId) {
        this.prevRule = ruleId;
    }

    public Long getNextRule() {
        return nextRule;
    }

    public void setNextRule(Long ruleId) {
        this.nextRule = ruleId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public StoredProject getProject() {
        return project;
    }

    public void setProject(StoredProject project) {
        this.project = project;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    /**
     * Returns the first rule in the invocation rules chain.
     * 
     * @param db the DB component's object
     * 
     * @return The <code>InvocationRule</code> DAO of the first rule in the
     *   chain, or <code>null</code> when the chain is empty or a database
     *   failure happened.
     */
    public static InvocationRule first(DBService db) {
        if (db == null) return null;
        HashMap<String,Object> properties = new HashMap<String, Object>();
        properties.put("prevRule", null);
        List<?> objects =
            db.doHQL("from " + InvocationRule.class.getName()
                    + " where prevRule is null");
        if ((objects != null) && (objects.size() > 0)) {
            return (InvocationRule) objects.get(0);
        }
        return null;
    }

    /**
     * Returns the rule that precedes the current one in the invocation rules
     * chain.
     * 
     * @param db the DB component's object
     * 
     * @return The <code>InvocationRule</code> DAO of the previous rule in the
     *   chain. Or <code>null</code> when the chain is empty, when this is the
     *   first rule, or if a database failure happened.
     */
    public InvocationRule prev(DBService db) {
        if (db == null) return null;
        if (getPrevRule() != null) {
            return db.findObjectById(InvocationRule.class, getPrevRule());
        }
        return null;
    }

    /**
     * Returns the rule that follows the current one in the invocation rules
     * chain.
     * 
     * @param db the DB component's object
     * 
     * @return The <code>InvocationRule</code> DAO of the next rule in the
     *   chain. Or <code>null</code> when the chain is empty, when this is the
     *   last rule, or if a database failure happened.
     */
    public InvocationRule next(DBService db) {
        if (db == null) return null;
        if (getNextRule() != null) {
            return db.findObjectById(InvocationRule.class, getNextRule());
        }
        return null;
    }

    /**
     * Returns the last rule in the invocation rules chain.
     * 
     * @param db the DB component's object
     * 
     * @return The <code>InvocationRule</code> DAO of the last rule in the
     *   chain, or <code>null</code> when the chain is empty or a database
     *   failure happened.
     */
    public static InvocationRule last(DBService db) {
        if (db == null) return null;
        HashMap<String,Object> properties = new HashMap<String, Object>();
        properties.put("nextRule", null);
        List<?> objects =
            db.doHQL("from " + InvocationRule.class.getName()
                    + " where nextRule is null");
        if ((objects != null) && (objects.size() > 0)) {
            return (InvocationRule) objects.get(0);
        }
        return null;
    }

    /**
     * Validates a singleton based scope against the given rule's value and
     * metric's type.
     * <br/>
     * The given metric's type determines the value content like:
     * <ul>
     *  <li><code>PROJECT_WIDE</code> and <code>SOURCE_CODE</code> expect a
     *  a single numeric project versions as a rule's value.
     * </ul>
     * 
     * @param value the rule's value
     * @param type the metric's type
     * 
     * @return <code>true</code> upon successful validation,
     *   or <code>false</code> otherwise.
     */
    private static boolean isSingleScope(String value, Type type) {
        if (value != null) {
            switch (type) {
            case PROJECT_WIDE:
            case SOURCE_CODE:
                try {
                    new Long(value); return true;
                }
                catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Validates a range based scope against the given rule's value and
     * metric's type.
     * <br/>
     * The given metric's type determines the value content like:
     * <ul>
     *  <li><code>PROJECT_WIDE</code> and <code>SOURCE_CODE</code> expect a
     *  hyphen ('-') separated range of numeric project versions
     *  (<i>exactly two</i>) as a rule's value.
     * </ul>
     * 
     * @param value the rule's value
     * @param type the metric's type
     * 
     * @return <code>true</code> upon successful validation,
     *   or <code>false</code> otherwise.
     */
    private static boolean isRangeScope(String value, Type type) {
        if (value != null) {
            switch (type) {
            case PROJECT_WIDE:
            case SOURCE_CODE:
                String[] values = value.split("-");
                if (values.length == 2) {
                    for (String nextVal : values) {
                        try {
                            new Long(nextVal);
                        }
                        catch (NumberFormatException ex) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Validates a list based scope against the given rule's value and
     * metric's type.
     * <br/>
     * The given metric's type determines the value content like:
     * <ul>
     *  <li><code>PROJECT_WIDE</code> and <code>SOURCE_CODE</code> expect a
     *  comma (',') separated list of numeric project versions
     *  (<i>at least two</i>) as a rule's value.
     * </ul>
     * 
     * @param value the rule's value
     * @param type the metric's type
     * 
     * @return <code>true</code> upon successful validation,
     *   or <code>false</code> otherwise.
     */
    private static boolean isListScope(String value, Type type) {
        if (value != null) {
            switch (type) {
            case PROJECT_WIDE:
            case SOURCE_CODE:
                String[] values = value.split(",");
                if (values.length > 1) {
                    for (String nextVal : values) {
                        try {
                            new Long(nextVal);
                        }
                        catch (NumberFormatException ex) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Validates the current rule.
     * 
     * @param db the DB component's object
     * 
     * @throws <code>Exception</code>, which describes the reason for the
     *   validation failure.
     */
    public void validate(DBService db) throws Exception {
        //====================================================================
        // Assemble the rule components
        //====================================================================
        // Assemble the metric type
        Type type = null;
        if (metricType != null) {
            type = Type.fromString(metricType.getType());
        }
        // Assemble the rule value
        String value = getValue();
        // Assemble the rule scope
        ScopeType scope = null;
        if (getScope() != null) {
            scope = ScopeType.fromString(getScope());
        }
        //====================================================================
        // Validate the rule's scope
        //====================================================================
        // Check for invalid ("null") scope 
        if (scope == null) {
            throw new Exception("Invalid scope type!");
        }
        // Check for selected scope without defined target
        if ((scope != ScopeType.ALL) && (type == null)) {
            throw new Exception("A scope is selected but a metric type"
                    + " is not defined!");
        }
        switch (scope) {
        case ALL:
            break;
        case EXACT:
        case EACH:
        case FROM:
        case TO:
            if (isSingleScope(value, type) == false) {
                throw new Exception("Invalid value for that scope!");
            }
            break;
        case RANGE:
            if (isRangeScope(value, type) == false) {
                throw new Exception(
                        "Invalid range of values for that scope!");
            }
            break;
        case LIST:
            if (isListScope(value, type) == false) {
                throw new Exception(
                        "Invalid list of values for that scope!");
            }
            break;
        default:
            throw new Exception("Unknown scope type!");
        }
    }

    /**
     * Compares this rule to the given one. This method returns
     * <code>true</code> only if the given object is not <code>null</code>
     * and the following rule fields are equal:
     * <ul>
     *   <li> project - integer comparison by project Id
     *   <li> plug-in - integer comparison by plug-in Id
     *   <li> metric type - string comparison by metric's type
     *   <li> scope - string comparison by rule's scope
     *   <li> action - string comparison by rule's action
     *   <li> value - string comparison by rule's value
     * </ul>
     * 
     * @param rule the rule to compare
     * 
     * @return <code>true</code>, if equal, or <code>false</code> otherwise.
     */
    public boolean equals(InvocationRule rule) {
        // Check for a valid input parameter
        if (rule == null) {
            return false;
        }
        // Compare the project
        if ((rule.getProject() != null) && (getProject() != null)) {
            if (rule.getProject().getId() != getProject().getId()) {
                return false;
            }
        }
        else if ((rule.getProject() != null) || (getProject() != null)) {
            return false;
        }
        // Compare the plug-in
        if ((rule.getPlugin() != null) && (getPlugin() != null)) {
            if (rule.getPlugin().getId() != getPlugin().getId()) {
                return false;
            }
        }
        else if ((rule.getPlugin() != null) || (getPlugin() != null)) {
            return false;
        }
        // Compare the metric type
        if ((rule.getMetricType() != null) && (getMetricType() != null)) {
            if (rule.metricType.getType().equals(
                    getMetricType().getType()) != true) {
                return false;
            }
        }
        else if ((rule.getMetricType() != null)
                || (getMetricType() != null)) {
            return false;
        }
        // Compare the scope
        if ((rule.getScope() != null) && (getScope() != null)) {
            if (rule.getScope().equals(getScope()) != true) {
                return false;
            }
        }
        else if ((rule.getScope() != null) || (getScope() != null)) {
            return false;
        }
        // Compare the action
        if ((rule.getAction() != null) && (getAction() != null)) {
            if (rule.getAction().equals(getAction()) != true) {
                return false;
            }
        }
        else if ((rule.getAction() != null) || (getAction() != null)) {
            return false;
        }
        // Compare the value
        if ((rule.getValue() != null) && (getValue() != null)) {
            if (rule.getValue().equals(getValue()) != true) {
                return false;
            }
        }
        else if ((rule.getValue() != null) || (getValue() != null)) {
            return false;
        }
        return true;
    }

    public boolean match(ScopeType scp, String val, ProjectFile res) {
        // Always match on "ALL" scope
        if (scp == ScopeType.ALL) return true;
        // Compare the rule value to the resource's project version
        long version = res.getProjectVersion().getVersion();
        // TODO: Check if the project file exists for other project versions
        switch (scp) {
        case EXACT:
        case EACH:
        case FROM:
        case TO:
            long value = parseIntValue(val);
            return (value == version);
        case RANGE:
            long[] range = parseIntRange(val);
            if ((range[0] <= version) && (version <= range[1]))
                return true;
            else 
                return false;
        case LIST:
            long[] values = parseIntList(val);
            for (int i = 0; i < values.length ; i++) {
                if (values[i] == version) return true;
            }
            return false;
        }
        // Unrecognized scope types are dealt here
        return false;
    }

    /**
     * Converts the given rule's value into a single integer.
     * 
     * @param val the rule's value
     * 
     * @return The integer representation of the rule's value.
     */
    private long parseIntValue(String val) {
        return new Long(val).longValue();
    }

    /**
     * Converts the given rule's value into a two member array of integer
     * values, where the least indexed integer represents the range begin and
     * the top indexed - the range end.
     * 
     * @param val the rule's value
     * 
     * @return The pair of integers representing the rule's values range.
     */
    private long[] parseIntRange(String val) {
        long[] result = new long[2];
        String[] values = val.split("-");
        long n1 = new Long(values[0]);
        long n2 = new Long(values[1]);
        if (n2 > n1) {
            result[0] = n1; result[1] = n2;
        }
        else {
            result[0] = n2; result[1] = n1;
        }
        return result;
    }

    /**
     * Converts the given rule's value into a list of integer values.
     * 
     * @param val the rule's value
     * 
     * @return The list of integers representing the rule's values set.
     */
    private long[] parseIntList(String val) {
        String[] values = val.split(",");
        long[] result = new long[values.length];
        for (int i = 0; i < values.length ; i++) {
            result[i] = new Long(values[i]);
        }
        return result;
    }
}
