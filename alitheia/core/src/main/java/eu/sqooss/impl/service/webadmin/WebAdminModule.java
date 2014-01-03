package eu.sqooss.impl.service.webadmin;

import javax.servlet.http.HttpServlet;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.sqooss.service.webadmin.WebadminService;

public class WebAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(WebadminService.class).to(WebadminServiceImpl.class);
		install(new FactoryModuleBuilder().implement(HttpServlet.class,
				AdminServlet.class).build(AdminServletFactory.class));
	}

}
