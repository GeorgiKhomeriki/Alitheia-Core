package eu.sqooss.impl.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleActivator;

import eu.sqooss.core.AlitheiaCore;

public class CoreActivator
    implements BundleActivator{

    public void start(BundleContext bc) throws Exception {
        AlitheiaCore core = new AlitheiaCore(bc);
        bc.registerService(core.getClass().getName(), core, null);

        // Run an instance of the WebAdmin
        core.initWebAdmin();
        
        // Run an instance of the PluginAdmin
        core.initPluginAdmin();
    }

    public void stop(BundleContext bc) throws Exception {
       
    }
}
