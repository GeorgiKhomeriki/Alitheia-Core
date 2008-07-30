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

import java.util.List;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PreferencesUtil;

import eu.sqooss.impl.plugin.Activator;
import eu.sqooss.impl.plugin.util.Constants;
import eu.sqooss.impl.plugin.util.Messages;
import eu.sqooss.plugin.util.EnabledState;

public class ConfigurationPropertyPage extends AbstractConfigurationPropertyPage
                                       implements SelectionListener,
                                                  TraverseListener,
                                                  IPropertyChangeListener {

    ControlEnableState enableProjectSpecific;
    
	public ConfigurationPropertyPage() {
		super();
	}

	
    /**
     * @see eu.sqooss.plugin.properties.AbstractConfigurationPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        mainControl = compositeProject;
        tabFolder.addSelectionListener(this);
        comboProjectVersion.addSelectionListener(this);
        spinnerServerPort.addTraverseListener(this);
        linkConfigurationPreferencePage.addSelectionListener(this);
        buttonProjectSpecificSettings.addSelectionListener(this);
        Activator.getDefault().getPreferenceStore().
            addPropertyChangeListener(this);
        enableIfPossible();
        synchronizeConnectionUtils(true);
        return control;
    }

    /**
     * @see eu.sqooss.plugin.properties.AbstractConfigurationPropertyPage#contributeButtons(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void contributeButtons(Composite parent) {
        super.contributeButtons(parent);
        buttonValidate.addSelectionListener(this);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        connectionUtils.setDefaultValues();
        synchronizeConnectionUtils(true);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (super.performOk()) {
            synchronizeConnectionUtils(false);
            connectionUtils.save();
            connectionUtils.validate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        synchronizeConnectionUtils(false);
        connectionUtils.save();
        notifyPropertyPages(connectionUtils.validate());
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (connectionUtils.isProjectSpecificAccount()) return;
        String property = event.getProperty();
        if (Constants.PREFERENCE_NAME_SERVER_ADDRESS.equals(property)) {
            connectionUtils.setServerAddress((String) event.getNewValue());
        } else if (Constants.PREFERENCE_NAME_SERVER_PORT.equals(property)) {
            connectionUtils.setServerPort(((Integer) event.getNewValue()).intValue());
        } else if (Constants.PREFERENCE_NAME_USER_NAME.equals(property)) {
            connectionUtils.setUserName((String) event.getNewValue());
        } else if (Constants.PREFERENCE_NAME_USER_PASSWORD.equals(property)) {
            connectionUtils.setPassword((String) event.getNewValue());
        }
        if (!mainControl.isDisposed()) {
            synchronizeConnectionUtils(true);
        }
    }
    
    public void keyTraversed(TraverseEvent e) {
        Object eventSource = e.getSource();
        if (eventSource == spinnerServerPort) {
            e.doit = e.keyCode != SWT.CR; // vetoes all CR traversals
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
        //do nothing
    }

    public void widgetSelected(SelectionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource == buttonValidate) {
            validateConfiguration();
        } else if (eventSource == tabFolder) {
            int selectedItemIndex = tabFolder.getSelectionIndex();
            if ((selectedItemIndex != -1) &&
                    (tabItemProject == tabFolder.getItem(selectedItemIndex))) {
                synchronizeConnectionUtils(false);
                connectionUtils.save();
                this.setEnabled(connectionUtils.validate(),
                        connectionUtils.getErrorMessage());
                if (connectionUtils.isValidAccount() &&
                        (controlEnableState != null)) {
                    controlEnableState.restore();
                    controlEnableState = null;
                }
            }
        } else if (eventSource == comboProjectVersion) {
            if (Messages.ConfigurationPropertyPage_Combo_Other_Project_Version.
                    equals(comboProjectVersion.getText())) {
                comboProjectVersion.setText("");
            }
        } else if (eventSource == linkConfigurationPreferencePage) {
            String id = eu.sqooss.plugin.util.Constants.CONFIGURATION_PREFERENCE_PAGE_ID;
            PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null).open();
        } else if (eventSource == buttonProjectSpecificSettings) {
            if ((buttonProjectSpecificSettings.getSelection() &&
                    (enableProjectSpecific != null))) {
                enableProjectSpecific.restore();
                enableProjectSpecific = null;
                linkConfigurationPreferencePage.setEnabled(false);
                connectionUtils.setProjectSpecificAccount(true);
            } else if ((!buttonProjectSpecificSettings.getSelection() &&
                    (enableProjectSpecific == null))) {
                enableProjectSpecific = ControlEnableState.disable(compositeAccount);
                linkConfigurationPreferencePage.setEnabled(true);
                connectionUtils.setProjectSpecificAccount(false);
                synchronizeConnectionUtils(true);
            }
        }
    }
	
    @Override
    public void setEnabled(boolean isEnabled, String errorMessage) {
        if (!connectionUtils.isValidAccount()) {
            super.setEnabled(isEnabled, errorMessage);
        } else if (!connectionUtils.isValidProjectVersion()) {
            super.setEnabled(true, connectionUtils.getErrorMessage());
        } else {
            super.setEnabled(true, null);
        }
        if (connectionUtils.isProjectSpecificAccount()) {
            buttonProjectSpecificSettings.setSelection(true);
        } else {
            buttonProjectSpecificSettings.setSelection(false);
        }
        buttonProjectSpecificSettings.notifyListeners(SWT.Selection, null);
    }

    @Override
    public boolean okToLeave() {
        synchronizeConnectionUtils(false);
        connectionUtils.save();
        notifyPropertyPages(connectionUtils.validate());
        return super.okToLeave();
    }


    private void validateConfiguration() {
        synchronizeConnectionUtils(false);
        boolean isValid = connectionUtils.validate();
        if (isValid) {
            boolean isForSave;
            isForSave = MessageDialog.openQuestion(getShell(),
                    Messages.ConfigurationPropertyPage_MessageBox_Validate_Title,
                    Messages.ConfigurationPropertyPage_MessageBox_Validate_Pass);
            if ((isForSave) && (!connectionUtils.save())) {
                MessageDialog.openWarning(getShell(),
                        Messages.ConfigurationPropertyPage_MessageBox_Save_Title,
                        Messages.ConfigurationPropertyPage_MessageBox_Save_Fail);
            }
        } else {
            MessageDialog.openWarning(getShell(),
                    Messages.ConfigurationPropertyPage_MessageBox_Validate_Title,
                    Messages.ConfigurationPropertyPage_MessageBox_Validate_Fail + 
                    "\n\nReason: " + connectionUtils.getErrorMessage());
        }
        notifyPropertyPages(isValid);
    }
    
    private void synchronizeConnectionUtils(boolean synchronizeGUI) {
        if (synchronizeGUI) {
            textFieldServerAddress.setText(connectionUtils.getServerAddress());
            spinnerServerPort.setSelection(connectionUtils.getServerPort());
            textFieldUserName.setText(connectionUtils.getUserName());
            textFieldPassword.setText(connectionUtils.getPassword());
            textFieldProjectName.setText(connectionUtils.getProjectName());
            comboProjectVersion.setText(connectionUtils.getProjectVersion());
        } else {
            connectionUtils.setServerAddress(textFieldServerAddress.getText());
            connectionUtils.setServerPort(spinnerServerPort.getSelection());
            connectionUtils.setProjectName(textFieldProjectName.getText());
            connectionUtils.setProjectVersion(comboProjectVersion.getText());
            connectionUtils.setUserName(textFieldUserName.getText());
            connectionUtils.setPassword(textFieldPassword.getText());
        }
    }
    
    private void notifyPropertyPages(boolean isEnabled) {
        PreferenceDialog preferenceDialog = (PreferenceDialog) getContainer();
        List<?> nodes = preferenceDialog.getPreferenceManager().getElements(PreferenceManager.POST_ORDER);
        IPreferencePage currentPage;
        for (Object node : nodes) {
            currentPage = ((IPreferenceNode) node).getPage();
            if (currentPage instanceof EnabledState) {
                ((EnabledState) currentPage).setEnabled(isEnabled,
                        connectionUtils.getErrorMessage());
            }
        }
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
