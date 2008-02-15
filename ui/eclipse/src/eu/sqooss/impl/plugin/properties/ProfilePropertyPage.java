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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import eu.sqooss.plugin.util.ConnectionUtils;
import eu.sqooss.plugin.util.Constants;
import eu.sqooss.plugin.util.EnabledState;

public class ProfilePropertyPage extends AbstractProfilePropertyPage implements EnabledState, SelectionListener {
    
    private ControlEnableState controlEnableState;
    
    /**
     * @see eu.sqooss.plugin.properties.AbstractProfilePropertyPage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        IResource resource = (IResource) (getElement().getAdapter(IResource.class));
        IProject resourceProject = resource.getProject();
        
        Composite containerComposite = (Composite) super.createContents(parent);
        
        configurationLink.addSelectionListener(this);
        
        textFieldPath.setText(getEntityPath());
        
        setEnabled(ConnectionUtils.validateConfiguration(resourceProject) == null);
        
        return containerComposite;
    }
    
    public void setEnabled(boolean isEnable) {
        if (isEnable) {
            //it is disabled before, enable now
            if (controlEnableState != null) {
                controlEnableState.restore();
                controlEnableState = null;
                configurationLink.setVisible(false);
            }
        }else {
            //it is enabled, disable now
            if (controlEnableState == null) {
                controlEnableState = ControlEnableState.disable(mainComposite);
                configurationLink.setVisible(true);
            }
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
        //do nothing
    }

    public void widgetSelected(SelectionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource == configurationLink) {
            IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer)getContainer();
            container.openPage(Constants.CONFIGURATION_PROPERTY_PAGE_ID, null);
        }
    }
    
    private String getEntityPath() {
        IResource resource = (IResource) (getElement().getAdapter(IResource.class));
        return ProjectConverterUtility.getEntityPath(resource);
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
