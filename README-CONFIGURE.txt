***
*** README-CONFIGURE
***

This README explains how to configure an Alitheia Core Platform for use.

***
*** Runtime configuration
***

The 'make run' target launches a local Alitheia system. You may optionally
add an argument JAVA_ARGS= to the command line specifying options to pass
to the JVM. Typical use would be something like this:

	make run JAVA_ARGS=-Xmx4096M

The following system properties are (at the very least) used:

log4j.configuration
	Set this to the URL of a log4j configuration file. The default
	is the log4j configuration file in equinox/configuration,
	which logs everything to a file and errors to the console.

***
*** Runtime configuration of OSGi
***

At runtime, the OSGi framework reads properties from the file config.ini
which is located in the equinox/configuration/ directory. Use standard
key=value pairs to define properties. The supported properties are
described in the file itself.

***
*** Running tests
***

Alitheia contains a run-time testing facility. The bundle tester
(installed by default) will run, on startup, the selfTest() method
defined in each service, but only if the startup tests are enabled
in the runtime configuration file. To enable them, set

eu.sqooss.tester.enable=true

When debugging in Eclipse, the switch to enable the tests can be
passed in as an argument to the JVM: Add 

       -Deu.sqooss.tester.enable=true

to "VM Arguments" under "Arguments" in the debug configuration used to
run the Alitheia OSGi bundles. 

Then each service object registered with the OSGi framework (on startup 
of the tester bundle) will be examined. Those with a selfTest() method 
will be called.

The selfTest() method should return null on success and an Object
which describes the failure (for instance, a String) on failure.
Failures are logged. Here is a sample:

	public Object selfTest() {
		// Normal test functions return true on success
		if (actualtestFunction()) {
			return null;
		} else {
			return new String("My actualTestFunction failed.");
		}
	}

This selfTest() method must be defined in the object which is passed
to BundleContext.registerService().



