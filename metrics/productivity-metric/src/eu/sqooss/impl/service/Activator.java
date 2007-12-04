package eu.sqooss.impl.service;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import eu.sqooss.impl.metrics.productivity.ProductivityMetricImpl;

public class Activator implements BundleActivator {

    private ServiceRegistration registration;

    public void start(BundleContext bc) throws Exception {

	Dictionary d = new Hashtable(2);
	
	d.put("metric.id", "");
	
        registration = bc.registerService(ProductivityMetricImpl.class
                .getName(), new ProductivityMetricImpl(bc), null);
    }

    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }
}
