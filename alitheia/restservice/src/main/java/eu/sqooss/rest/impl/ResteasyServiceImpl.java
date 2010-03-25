package eu.sqooss.rest.impl;

import javax.servlet.ServletContext;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import eu.sqooss.rest.ResteasyService;

/**
 * ResteasyService provides interface to the internal RESTEasy implementation.
 * @author <a href="mailto:baldin@gmail.com">Davi Baldin H. Tavares</a> 
 */
public class ResteasyServiceImpl implements ResteasyService {
	
	private ServletContext context;
	
	public ResteasyServiceImpl(ServletContext context) {
		this.context = context;
	}
	
	public ResteasyProviderFactory getResteasyProviderFactory() {
		if (context != null) {
			return (ResteasyProviderFactory) context.getAttribute(ResteasyProviderFactory.class.getName());
		}else{
			return null;
		}
	}
	
	public Dispatcher getDispatcher() {
		if (context != null) {
			return (Dispatcher) context.getAttribute(Dispatcher.class.getName());
		}else{
			return null;
		}

	}
	
	public Registry getRegistry() {
		if (context != null) {
			return (Registry) context.getAttribute(Registry.class.getName());
		}else{
			return null;
		}
	}
	
	public void addSingletonResource(Object resource) {
		getRegistry().addSingletonResource(resource);
	}
	
	
	public void removeSingletonResource(Class<?> clazz) {
		getRegistry().removeRegistrations(clazz);
	}
	
}