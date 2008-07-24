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

package eu.sqooss.impl.plugin.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import eu.sqooss.impl.plugin.util.Messages;
import eu.sqooss.impl.plugin.util.visualizers.Visualizer;
import eu.sqooss.impl.plugin.util.visualizers.VisualizerFactory;
import eu.sqooss.impl.plugin.util.visualizers.VisualizerFactory.Type;
import eu.sqooss.plugin.util.ConnectionUtils;
import eu.sqooss.plugin.util.Constants;
import eu.sqooss.plugin.util.Entity;
import eu.sqooss.ws.client.datatypes.WSMetric;
import eu.sqooss.ws.client.datatypes.WSResultEntry;

public class QualityPropertyPage extends AbstractQualityPropertyPage
                                 implements SelectionListener,
                                            Listener,
                                            TraverseListener {

    private static final int VERSIONS_MAX_NUMBER = 29;
    private static final char INTERVAL_DELIMITER = '-';
    private static final String VERSION_PREFIX   =
        Messages.QualityPropertyPage_Combo_Compare_Version_Prefix;
    private static final String VERSION_CURRENT_POSTFIX =
        Messages.QualityPropertyPage_Combo_Compare_Version_Postfix;
    
    private Composite parent;
    private Entity entity;
    private Visualizer visualizer;
    private int selectedMetricIndex;
    private boolean isClearedMetricResult;
    
    /**
     * @see eu.sqooss.plugin.properties.AbstractQualityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        this.parent = parent;
        mainControl = (Composite) super.createContents(parent);
        buttonCompareVersion.addSelectionListener(this);
        comboCompareVersion.addSelectionListener(this);
        comboCompareVersion.addTraverseListener(this);
        comboMetric.addSelectionListener(this);
        comboMetric.addListener(SWT.MouseDown, this);
        comboMetric.addListener(SWT.MouseUp, this);
        parent.forceFocus();
        enableIfPossible();
        processSelectedResult(false);
        return mainControl;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        noDefaultAndApplyButton();
        super.createControl(parent);
    }

    /**
     * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
     */
    public void keyTraversed(TraverseEvent e) {
        e.doit = e.keyCode != SWT.CR; // vetoes all CR traversals
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource == comboCompareVersion) {
            processEnteredResult();
        }
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        Object eventSource = e.getSource(); 
        if (eventSource == buttonCompareVersion) {
            boolean isEnabledComboCompare = comboCompareVersion.isEnabled();
            comboCompareVersion.setEnabled(!isEnabledComboCompare);
            comboCompareVersion.deselectAll();
            this.visualizer.close();
            this.visualizer = null;
            processSelectedResult(false);
        } else if (eventSource == configurationLink) {
            IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer)getContainer();
            container.openPage(Constants.CONFIGURATION_PROPERTY_PAGE_ID, null);
        } else if (eventSource == comboMetric) {
            comboCompareVersion.deselectAll();
            boolean isSame = comboMetric.getSelectionIndex() == selectedMetricIndex;
            processSelectedResult(isSame && !isClearedMetricResult);
        } else if (eventSource == comboCompareVersion) {
            String comboText = comboCompareVersion.getText();
            if ((Messages.QualityPropertyPage_Combo_Compare_Version_Interval.equals(comboText)) ||
                    (Messages.QualityPropertyPage_Combo_Compare_Version_Other.equals(comboText))) {
                comboCompareVersion.setText("");
            } else {
                processSelectedResult(false);
            }
        }
    }
    
    /**
     * @see eu.sqooss.impl.plugin.properties.EnabledPropertyPage#setEnabled(boolean, java.lang.String)
     */
    public void setEnabled(boolean isEnabled, String errorMessage) {
        if (mainControl == null) return; //the method createContents isn't called yet
        super.setEnabled(isEnabled, errorMessage);
        if (isEnabled) {
            String internalErrorMessage = fill(); 
            if (internalErrorMessage != null) {
                setEnabled(false, internalErrorMessage);
                return;
            }
            if (configurationLink != null) {
                //remove the configuration link
                configurationLink.dispose();
                configurationLink = null;
                parent.layout();
            }
            processSelectedResult(false);
        }else {
            if (configurationLink == null) {
                //add configuration link
                configurationLink = new Link(parent, SWT.NONE);
                configurationLink.setText(Messages.ProjectPropertyPage_Link_Configuration);
                configurationLink.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
                configurationLink.addSelectionListener(this);
                parent.layout();
            }
        }
    }
    
    /*
     * The method fills the GUI with the information of the entity.
     */
    private String fill() {
        if (!initEntity()) {
            return Messages.QualityPropertyPage_Message_Error_Missing_Entity;
        }
        textFieldEntityPath.setText(this.entity.getName());
        WSMetric[] metrics = this.entity.getMetrics();
        if ((metrics == null) || (metrics.length == 0)) {
            return Messages.QualityPropertyPage_Message_Error_Missing_Metrics;
        }
        comboMetric.removeAll();
        comboCompareVersion.removeAll();
        WSMetric currentMetric;
        String currentItem;
        for (int i = 0; i < metrics.length; i++) {
            currentMetric = metrics[i];
            currentItem = currentMetric.getMnemonic() + " (" +
            currentMetric.getDescription() + ")";
            comboMetric.add(currentItem, i);
            comboMetric.setData(Integer.toString(i), currentMetric);
        }
        Long[] versions = this.entity.getSortedVersions();
        if ((versions != null) && (versions.length != 0)) {
            int j = 0;
            //add interval item
            comboCompareVersion.add(
                    Messages.QualityPropertyPage_Combo_Compare_Version_Interval, j++);
            //add configured version
            currentItem = VERSION_PREFIX +
            this.entity.getCurrentVersion() + VERSION_CURRENT_POSTFIX;
            if (this.entity.getCurrentVersion() >= versions[versions.length-1]) {
                currentItem += Messages.QualityPropertyPage_Combo_Compare_Version_Last;
            }
            comboCompareVersion.add(currentItem, j);
            comboCompareVersion.setData(Integer.toString(j),
                    this.entity.getCurrentVersion());
            j++;
            //add others
            Long currentVersion;
            if (versions.length > VERSIONS_MAX_NUMBER) {
                //add first version
                currentVersion = versions[0];
                currentItem  = VERSION_PREFIX + currentVersion +
                Messages.QualityPropertyPage_Combo_Compare_Version_First;
                comboCompareVersion.add(currentItem, j);
                comboCompareVersion.setData(Integer.toString(j), currentVersion);
                ++j;
                //add other item
                comboCompareVersion.add(Messages.
                        QualityPropertyPage_Combo_Compare_Version_Other, j++);
                //add last version
                currentVersion = versions[versions.length -1];
                if (this.entity.getCurrentVersion() < currentVersion) {
                    currentItem  = VERSION_PREFIX + currentVersion +
                    Messages.QualityPropertyPage_Combo_Compare_Version_Last;
                    comboCompareVersion.add(currentItem, j);
                    comboCompareVersion.setData(Integer.toString(j), currentVersion);
                    ++j;
                }
            } else {
                for (int i = 0; i < versions.length; i++) {
                    currentVersion = versions[i];
                    currentItem  = VERSION_PREFIX + currentVersion;
                    comboCompareVersion.add(currentItem, j);
                    comboCompareVersion.setData(Integer.toString(j), currentVersion);
                    j++;
                }
            }
        } else {
            buttonCompareVersion.setEnabled(false);
        }
        comboMetric.select(0);
        return null;
    }
    
    /*
     * The method sets the resource entity.
     */
    private boolean initEntity() {
        IResource resource = (IResource) (getElement().getAdapter(IResource.class));
        ConnectionUtils connectionUtils;
        try {
            connectionUtils = (ConnectionUtils) resource.getProject().
            getSessionProperty(ConnectionUtils.PROPERTY_CONNECTION_UTILS);
        } catch (CoreException e) {
            connectionUtils = null;
        }
        if ((connectionUtils == null) ||
                (!connectionUtils.isValidProjectVersion())) {
            return false;
        } else {
            this.entity = connectionUtils.getEntity(resource);
            return (this.entity == null) ? false : true;
        }
    }
    
    /*
     * The method is used by the result visualization.
     */
    private void processSelectedResult(boolean clearResult) {
        if (controlEnableState != null) return; //the control is disabled
        int selectedIndex = comboCompareVersion.getSelectionIndex();
        Long[] selectedVersions;
        if (selectedIndex == -1) {
            selectedVersions = new Long[] {this.entity.getCurrentVersion()};
        } else {
            selectedVersions = new Long[] {
                    (Long) comboCompareVersion.getData(Integer.toString(selectedIndex))};
            if (selectedVersions[0] == null) return;
        }
        if (clearResult) {
            visualizeResult(null, true);
        } else {
            visualizeResult(selectedVersions, false);
        }
        this.isClearedMetricResult = clearResult;
    }
    
    private void processEnteredResult() {
        String comboText = comboCompareVersion.getText().trim();
        //is number
        try {
            Long version = Long.valueOf(comboText);
            visualizeResult(new Long[] {version}, false);
        } catch (NumberFormatException nfe) {
            //go ahead and check the interval
        }
        //is interval
        int delimiterFirstIndex = comboText.indexOf(INTERVAL_DELIMITER);
        int delimiterLastIndex = comboText.lastIndexOf(INTERVAL_DELIMITER);
        if ((delimiterFirstIndex == -1) ||
                (delimiterFirstIndex != delimiterLastIndex)) return;
        Long[] selectedVersions;
        long fromVersion;
        long toVersion;
        try {
            fromVersion = Long.valueOf(comboText.substring(0, delimiterFirstIndex)).longValue();
        } catch (Exception e) {
            fromVersion = -1;
        }
        try {
            toVersion = Long.valueOf(comboText.substring(delimiterFirstIndex + 1, comboText.length())).longValue();
        } catch (Exception e) {
            toVersion = -1;
        }
        
        boolean isFirstDelimiter = delimiterFirstIndex == 0;
        boolean isLastDelimiter = delimiterFirstIndex == (comboText.length() - 1);
        if (((isLastDelimiter && (fromVersion == -1)) ||   //invalid  "X-"
                (isFirstDelimiter && (toVersion == -1)) || //invalid "-Y"
                (((fromVersion == -1) || (toVersion == -1)) && !isFirstDelimiter && !isLastDelimiter))//invalid "X-Y"
                && (comboText.length() != 1)) {
            visualizeResult(null, true);
            return;
        }
        selectedVersions = getVersions(fromVersion, toVersion);
        visualizeResult(selectedVersions, true);
    }
    
    private void visualizeResult(Long[] versions, boolean clearResult) {
        setVisualizer();
        String metricKey = Integer.toString(comboMetric.getSelectionIndex());
        WSMetric metric = (WSMetric) comboMetric.getData(metricKey);
        if (clearResult) {
            this.visualizer.removeMetricValues(metric.getMnemonic());
        }
        if ((versions == null) || (versions.length == 0)) return;
        WSResultEntry[] result = this.entity.getMetricsResults(
                new WSMetric[] {metric}, versions);
        if ((result != null) && (result.length != 0)) {
            for (WSResultEntry currentEntry : result) {
                this.visualizer.setValue(Long.valueOf(this.entity.getVersionById(currentEntry.getDaoId())),
                        currentEntry);
            }
        }
        this.visualizer.open();
    }
    
    private Long[] getVersions(long fromVersion, long toVersion) {
        if (toVersion < 0) toVersion = Long.MAX_VALUE;
        if (fromVersion > toVersion) {
            long tmp = fromVersion;
            fromVersion = toVersion;
            toVersion = tmp;
        }
        List<Long> selectedVersions = new ArrayList<Long>();
        Long[] entityVersions = this.entity.getSortedVersions();
        for (Long version : entityVersions) {
            if ((fromVersion <= version) && (version <= toVersion)) {
                selectedVersions.add(version);
            }
        }
        if ((fromVersion <= this.entity.getCurrentVersion().longValue()) &&
                (toVersion >= this.entity.getCurrentVersion().longValue())) {
            selectedVersions.add(this.entity.getCurrentVersion());
        }
        return selectedVersions.toArray(new Long[selectedVersions.size()]);
    }
    
    private void setVisualizer() {
        if (this.visualizer == null) {
            Type type = (comboCompareVersion.isEnabled()) ?
                    Type.CHART_LINE_SERIES : Type.TABLE;
            this.visualizer = VisualizerFactory.createVisualizer(
                    type, resultComposite, null, null);
        }
    }

    public void handleEvent(Event event) {
        Widget eventSource = event.widget;
        if (eventSource == comboMetric) {
            selectedMetricIndex = comboMetric.getSelectionIndex();
            comboMetric.deselect(selectedMetricIndex);
        }
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
