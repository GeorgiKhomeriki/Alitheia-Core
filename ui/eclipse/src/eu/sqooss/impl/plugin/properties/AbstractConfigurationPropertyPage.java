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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import eu.sqooss.impl.plugin.util.Messages;
import eu.sqooss.plugin.util.ConnectionUtils;

abstract class AbstractConfigurationPropertyPage extends EnabledPropertyPage {

    protected static final int SERVER_PORT_MIN = 0;
    protected static final int SERVER_PORT_MAX = 65535;
    
    private static final int TEXT_FIELDS_SWT_STYLE = SWT.SINGLE | SWT.BORDER;
    
    protected Text textFieldServerAddress;
    protected Spinner spinnerServerPort;
    protected Text textFieldUserName;
    protected Text textFieldPassword;
    protected Text textFieldProjectName;
    protected Combo comboProjectVersion;
    protected TabFolder tabFolder;
    protected TabItem tabItemProject;
    protected Composite compositeProject;
    
    protected Button buttonValidate;
    
    public AbstractConfigurationPropertyPage() {
        super();
    }
    
    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = createComposite(parent, 2);
        GridData compositeGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        composite.setLayoutData(compositeGridData);
        tabFolder = addTabFolder(composite);
        addAccountTabItem(tabFolder);
        addProjectTabItem(tabFolder);
        return composite;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#contributeButtons(org.eclipse.swt.widgets.Composite)
     */
    protected void contributeButtons(Composite parent) {
        super.contributeButtons(parent);
        ((GridLayout) parent.getLayout()).numColumns++;
        buttonValidate = new Button(parent, SWT.PUSH);
        buttonValidate.setText(Messages.ConfigurationPropertyPage_Button_Validate);
    }

    private TabFolder addTabFolder(Composite composite) {
        //create the tab folder
        TabFolder tabFolder = new TabFolder(composite, SWT.TOP);
        setLayoutData(tabFolder, 1);
        return tabFolder;
    }
    
    
    private void addAccountTabItem(TabFolder tabFolder) {
        Composite compositeAccount = createComposite(tabFolder, 4);
        setLayoutData(compositeAccount, 1);
        
        // add server url's components
        Label labelServerAddress = new Label(compositeAccount, SWT.NONE);
        labelServerAddress.setText(Messages.ConfigurationPropertyPage_Label_Server_Address);
        textFieldServerAddress = new Text(compositeAccount, TEXT_FIELDS_SWT_STYLE);
        setLayoutData(textFieldServerAddress, 1);
        Label labelServerPort = new Label(compositeAccount, SWT.NONE);
        labelServerPort.setText(Messages.ConfigurationPropertyPage_Label_Server_Port);
        spinnerServerPort = new Spinner(compositeAccount, SWT.NONE);
        spinnerServerPort.setValues(
                ConnectionUtils.SERVER_PORT_DEFAULT_VALUE, //set selection
                SERVER_PORT_MIN, //set minimum
                SERVER_PORT_MAX, //set maximum
                0,               //set digits
                1,               //set increment
                1);              //set page increment
        /*
         * The behavior of the spinner is not system independent.
         * Here are some related bugs:
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=186634
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100363
         * They have fixes on the windows.
         */
        
        // add user name's components
        Label labelUserName = new Label(compositeAccount, SWT.NONE);
        labelUserName.setText(Messages.ConfigurationPropertyPage_Label_User_Name);
        textFieldUserName = new Text(compositeAccount, TEXT_FIELDS_SWT_STYLE);
        setLayoutData(textFieldUserName, 3);

        // add password's components
        Label labelPassword = new Label(compositeAccount, SWT.NONE);
        labelPassword.setText(Messages.ConfigurationPropertyPage_Label_Password);
        textFieldPassword = new Text(compositeAccount, TEXT_FIELDS_SWT_STYLE | SWT.PASSWORD);
        setLayoutData(textFieldPassword, 3);
        
        TabItem tabItemAccount = new TabItem(tabFolder, SWT.NONE);
        tabItemAccount.setText(Messages.ConfigurationPropertyPage_TabItem_Account);
        tabItemAccount.setControl(compositeAccount);
    }
    
    private void addProjectTabItem(TabFolder tabFolder) {
        compositeProject = createComposite(tabFolder, 2);
        setLayoutData(compositeProject, 1);
        
        // add project's components - name
        Label labelProjectName = new Label(compositeProject, SWT.NONE);
        labelProjectName.setText(Messages.ConfigurationPropertyPage_Label_Project_Name);
        textFieldProjectName = new Text(compositeProject, TEXT_FIELDS_SWT_STYLE);
        setLayoutData(textFieldProjectName, 1);
        
        // add project's components - version
        Label labelProjectVersion = new Label(compositeProject, SWT.NONE);
        labelProjectVersion.setText(Messages.ConfigurationPropertyPage_Label_Project_Version);
        comboProjectVersion = new Combo(compositeProject, SWT.DROP_DOWN);
        setLayoutData(comboProjectVersion, 1);
        String[] items = new String[] {
                Messages.ConfigurationPropertyPage_Combo_First_Project_Version,
                Messages.ConfigurationPropertyPage_Combo_Other_Project_Version,
                Messages.ConfigurationPropertyPage_Combo_Last_Project_Version
        };
        comboProjectVersion.setItems(items);
        
        tabItemProject = new TabItem(tabFolder, SWT.NONE);
        tabItemProject.setText(Messages.ConfigurationPropertyPage_TabItem_Project);
        tabItemProject.setControl(compositeProject);
    }
    
    private Composite createComposite(Composite parent, int numColumns) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        composite.setLayout(layout);

        return composite;
    }
    
    private static void setLayoutData(Control control, int horizontalSpan) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = horizontalSpan;
        control.setLayoutData(gridData);
    }
    
}

//vi: ai nosi sw=4 ts=4 expandtab
