package eu.sqooss.impl.service.webadmin;

import org.osgi.framework.BundleContext;

import eu.sqooss.service.logging.Logger;

public interface AdminServletFactory {
	AdminServlet create(BundleContext bc, Logger logger);
}