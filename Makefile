# This is a GNU Makefile

# This Makefile is part of the Alitheia system produced by the SQO-OSS
# consortium and is covered by the same LICENSE as the rest of the system:
# the 2-Clause FreeBSD license which you may find in the LICENSE file.

# This top-level Makefile allows you to build the Alitheia system (the
# core system, also sometimes called the cruncher) with either Maven
# or with Make. The latter is much much faster, but might miss stuff.
# The Make system is the default; to switch on Maven builds, set
# WITH_MAVEN to a non-empty value, for instance like so:
#	make build install run WITH_MAVEN=YES
#
# The top-level targets are the following:
#
# build		- Compile all of the Java sources into jars for each bundle.
# install	- Install the resulting bundles into the equinox dir.
# run		- Run the OSGi / Equinox system.
# run-bg	- Run the OSGi / Equinox system without a console and in
#             	background mode.
# run-ui	- Start up tomcat with the public-facing web front end.
# stop-ui	- Stop the tomcat instance.
# start		- Run the web UI and the core system.
# start-bg	- Run the web UI and the core system (the latest without a
#             console and in BG mode).
# clean		- Remove all build artifacts and logs.
# clean-log	- Clean up just the logs. Keeps show-log short.
# clean-db	- Remove the Derby DB, so it will be re-created next run.
#		  Only useful if you are using Derby, which is the fallback
#		  when Postgres can't be found.
# show-log	- Finds the run log and prints it.
# show-db	- Start the Derby CLI for database manipulation.
#		  Only useful if you are using Derby, which is the fallback
#		  when Postgres can't be found.
# show-db-tables - shows the generated database tables. Also Derby-only.
# fill-db-tables - install canned data
# clean-db-tables - clear the database using delete statements; this keeps
#		    the structure (unlike clean-db).
#
# eclipse-up-branch  - Update the eclipse branch from the current workdir
# eclipse-up-workdir - Update the current workdir from the eclipse branch
#
# After 'make run' you may need to type 'close' on the OSGi console
# to quit the Alitheia system (in production circumstances you would
# not have the console). The run target assumes (and does not check)
# that you have done a 'make install' already.

# This is where OSGi / Equinox is installed under this directory.
PREFIX=equinox

# Subdirectories to build or install from.
SUBDIRS= sharedlibs \
	alitheia \
	metrics
ifeq ($(WITH_MAVEN),)
SUBDIRS+= corba
endif

CLASSPATH=$(shell tools/setcp.sh `pwd`)
ifeq ($(OS),Windows_NT)
CLASSPATH:=$(subst /,\,$(CLASSPATH))
CLASSPATH:=$(subst :,;,$(CLASSPATH))
endif


#
# END OF USER CONFIGURATION AREA
#
###

TOP_SRCDIR=$(shell pwd)
ABS_PREFIX=$(shell cd $(PREFIX) && pwd)
LOG4J_PREFIX=$(ABS_PREFIX)
ifeq ($(OS),Windows_NT)
LOG4J_PREFIX:=$(subst /cygdrive/c,,$(LOG4J_PREFIX))
endif

all : build

# Template to carry a target to a subdirectory while preserving the
# PREFIX and Maven attributes.
define subdir_template
$(1)-$(2) :
	cd $(2) && $(MAKE) $(1) TOP_SRCDIR=$(TOP_SRCDIR) PREFIX=$(ABS_PREFIX) WITH_MAVEN=$(WITH_MAVEN)
endef

$(foreach d,$(SUBDIRS),$(eval $(call subdir_template,build,$(d))))
$(foreach d,$(SUBDIRS),$(eval $(call subdir_template,clean,$(d))))
$(foreach d,$(SUBDIRS),$(eval $(call subdir_template,install,$(d))))



build : $(foreach d,$(SUBDIRS),build-$(d))

install : $(foreach d,$(SUBDIRS),install-$(d))
	cd extlibs && $(MAKE) TOP_SRCDIR=$(TOP_SRCDIR) && $(MAKE) install TOP_SRCDIR=$(TOP_SRCDIR)
	rm -Rf ${PREFIX}/configuration/org.eclipse.osgi
	rm -f ${PREFIX}/configuration/*.log

TOOL_DIR=tools
# None of the tools in the tools dir need to be used right now,
# so there are no targets referencing it.


clean : clean-log $(foreach d,$(SUBDIRS),clean-$(d))
	rm -rf $(PREFIX)/configuration/org.eclipse.osgi
	rm -f $(PREFIX)/eu.sqooss.service.*.jar \
		$(PREFIX)/eu.sqooss.metrics.*.jar
	rm -f $(PREFIX)/*.jar

clean-log :
	rm -f $(PREFIX)/alitheia.log $(PREFIX)/hibernate.log $(PREFIX)/derby.log
	rm -f $(PREFIX)/logs/*

clean-db :
	rm -rf $(PREFIX)/derbyDB

distclean: clean clean-log clean-db
	-find . -type f|grep *~|xargs rm
	-find . -type f|grep DS_Store|xargs rm 

javadoc:
	ALLSRC=`find . -type f -name "*.java"|tr '\n' ' '` && javadoc -d doc/javadoc -classpath `./tools/setcp.sh .` $$ALLSRC

#Just a dummy config file
CONFIG=-Xmx256M

DEBUGOPT=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y 

CL_CONFIG=-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger
LOG4J_CONFIG=-Dlog4j.configuration=file://$(LOG4J_PREFIX)/configuration/log4j.properties
JETTY_CONFIG=-DDEBUG_VERBOSE=1 -DDEBUG_PATTERNS=main,org.mortbay.http -Dorg.mortbay.log.LogFactory.noDiscovery=false

# $(CONFIG) would typically be used to set system properties.
run :
	cd $(PREFIX) && \
	java $(CONFIG) \
		-DDEBUG $(CL_CONFIG) $(LOG4J_CONFIG) $(JETTY_CONFIG) \
		-jar org.eclipse.osgi_3.3.0.v20070321.jar -console

run-bg :
	cd $(PREFIX) && \
	java $(CONFIG) \
		-DDEBUG $(CL_CONFIG) $(LOG4J_CONFIG) $(JETTY_CONFIG) \
		-jar org.eclipse.osgi_3.3.0.v20070321.jar -no-exit &

debug :
	cd $(PREFIX) && \
	java $(DEBUGOPT) $(CONFIG) \
		-DDEBUG $(CL_CONFIG) $(LOG4J_CONFIG) $(JETTY_CONFIG) \
		-jar org.eclipse.osgi_3.3.0.v20070321.jar -console 

run-ui :
	cd ui/webui && $(MAKE) start

stop-ui :
	cd ui/webui && $(MAKE) stop

start : run-ui run

start-bg : run-ui run-bg


# The default log4j configuration puts the log directly in $(PREFIX) and
# the SQO-OSS logger puts it in the bundle data directory. Handle both.
show-log :
	if test -s $(PREFIX)/logs/alitheia.log  ; then \
		cat $(PREFIX)/logs/alitheia.log ; \
	else \
		cat $(PREFIX)/configuration/org.eclipse.osgi/bundles/[0-9]*/data/logs/alitheia*.log ; \
	fi

DBPATH=extlibs/org.apache.derby_10.3.2.1
RUN_DERBY_CLASSPATH=$(DBPATH)/derby.jar:$(DBPATH)/../org.apache.derby.tools-10.3.1.4.jar
ifeq ($(OS),Windows_NT)
RUN_DERBY_CLASSPATH:=$(subst /,\,$(RUN_DERBY_CLASSPATH))
RUN_DERBY_CLASSPATH:=$(subst :,;,$(RUN_DERBY_CLASSPATH))
endif
RUN_DERBY_IJ:=java -Dij.protocol=jdbc:derby: -Dij.database=equinox/derbyDB -cp "$(RUN_DERBY_CLASSPATH)" org.apache.derby.tools.ij

show-db :
	$(RUN_DERBY_IJ)

show-db-tables :
	echo "show tables;" | $(RUN_DERBY_IJ) | grep '^ALITHEIA'

fill-db-tables :
	cat examples/db.sql | $(RUN_DERBY_IJ) 

clean-db-tables :
	( echo "delete from alitheia.mailmessage;" ; \
 	echo "delete from alitheia.sender;" ; \
	echo "delete from alitheia.mailinglist;" ; \
	echo "delete from alitheia.stored_project;" ) | $(RUN_DERBY_IJ)

ECLIPSEDIR=$(TOP_SRCDIR)/../branches/eclipse

eclipse-up-branch: distclean
	@if [ !  "`svn status|grep -v "^?"`" ]; then \
		echo **Modified exist, take care of them first;\
		exit ; \
	fi &&\
	files=`svn st|grep ^?|tr -s ' '|cut -f2 -d' '|tr '\n' ' '` && \
	for file in $$files; do \
		mkdir -p ../tmp/`dirname $$file`;  \
		cp -rv $$file ../tmp/$$file;  \
	done && \
	rsync -rv ../tmp/ $(ECLIPSEDIR) && \
	echo Cleaning up.... && \
	rm -R ../tmp &&\
	echo "#################################" && \
	echo "#Commit the following files/dirs#" && \
	echo "#################################" && \
	svn st $(ECLIPSEDIR) 

eclipse-up-workdir: distclean
	if [ ! -z "`svn status|grep -v "^?"`" ]; then \
		echo Modified or added files are in place, take care of them first;\
		exit ; \
	fi && \
	(cd $(ECLIPSEDIR) && svn up) && \ 
	rsync -rv --exclude ".svn" $(ECLIPSEDIR)/ $(TOP_SRCDIR)/ 

