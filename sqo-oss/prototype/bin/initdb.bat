
set LIB="%SQOOSS_HOME%/lib"
set CP="%SQOOSS_HOME%/bin;%LIB%/antlr-2.7.6.jar;%LIB%/asm.jar;%LIB%/c3p0-0.9.1.jar;%LIB%/cglib-2.1.3.jar;%LIB%/commons-cli.jar;%LIB%/commons-collections-2.1.1.jar;%LIB%/commons-logging.jar;%LIB%/dom4j-1.6.1.jar;%LIB%/hibernate-tools.jar;%LIB%/hibernate3.jar;%LIB%/hsqldb.jar;%LIB%/jta.jar;%LIB%/junit-3.8.1.jar;%LIB%/svnkit.jar;%LIB%/log4j-1.2.14.jar;%SQOOSS_HOME%/dist"
set SQOOSS_JAR="%SQOOSS_HOME%/dist/sqo-oss.jar"

java -classpath %SQOOSS_JAR%;%CP%;"%CLASSPATH%" eu.sqooss.db.InitDB %1 %2 %3 %4 %5 %6 %7 %8 %9