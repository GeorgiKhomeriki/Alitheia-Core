package eu.sqooss.impl.service.webadmin;

import javax.servlet.http.HttpServlet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.webadmin.WebadminService;

public class WebAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(WebadminService.class).to(WebadminServiceImpl.class);
		install(new FactoryModuleBuilder().implement(HttpServlet.class,
				AdminServlet.class).build(AdminServletFactory.class));
	}

	@Provides
	VelocityEngine provideVelocityEngine() {
		VelocityEngine ve = null;
		try {
			ve = new VelocityEngine();
			ve.setProperty("runtime.log.logsystem.class",
					"org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			ve.setProperty("runtime.log.logsystem.log4j.category",
					Logger.NAME_SQOOSS_WEBADMIN);
			String resourceLoader = "classpath";
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
			ve.setProperty(resourceLoader + "."
					+ RuntimeConstants.RESOURCE_LOADER + ".class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		} catch (Exception e) {
			System.err.println("Error: Velocity initialization");
			e.printStackTrace();
		}
		return ve;
	}
	
	@Provides
	VelocityContext provideVelocityContext() {
		return new VelocityContext();
	}

}
