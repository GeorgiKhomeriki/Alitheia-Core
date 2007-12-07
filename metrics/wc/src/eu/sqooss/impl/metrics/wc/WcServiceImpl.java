package eu.sqooss.impl.metrics.wc;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.logging.Logger;

public class WcServiceImpl {
    private static final long serialVersionUID = 1L;

    private ServiceReference serviceRef = null;

    private LogManager logService = null;

    private Logger logger = null;

    public WcServiceImpl(BundleContext bc)  {
        /*Get a reference to the logging service*/
    	serviceRef = bc.getServiceReference(AlitheiaCore.class.getName());
    	logService = ((AlitheiaCore) bc.getService(serviceRef)).getLogManager();

        if (logService != null) {
            logger = logService.createLogger(Logger.NAME_SQOOSS_UPDATER);

            if (logger != null)
                logger.info("Got a valid reference to the logger");
        }

        if (logger == null) {
            System.out.println("ERROR: Got no logger");
        }
    }
}
