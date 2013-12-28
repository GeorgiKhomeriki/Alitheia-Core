package eu.sqooss.core;

//import static org.ops4j.peaberry.Peaberry.service;
//import static org.ops4j.peaberry.util.TypeLiterals.export;

import com.google.inject.AbstractModule;

import eu.sqooss.impl.service.tds.TDSServiceImpl;
import eu.sqooss.service.tds.TDSService;

public class AlitheiaCoreModule extends AbstractModule {

	@Override
	protected void configure() {
		System.out.println("*** AlitheiaCoreModule.configure() called");
		// note: the service is exported to the registry at injection time
		bind(TDSService.class).to(TDSServiceImpl.class);
		//bind(export(TDSService.class)).toProvider(service(new TDSServiceImpl()).export());
	}

}
