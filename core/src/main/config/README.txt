This directory contains various versions of screensaver.properties and
log4j.properties files, to support multiple build-time and run-time
configurations for different development, testing, and deployment 
environments.  These files are example files and should be edited to suit
your needs.


screensaver.properties:

This file is used to set both build-time and run-time settings.  The
build-time settings are used by Ant, and are found by specifying the
'screensaver.properties.file' property on the Ant command-line.  You can
define any Ant properties you want to in this file.  For web deployments and
command-line utility distributions, this file is also copied into the
resources directory, allowing its run-time properties to be made available to
the running application.  You may also want to place a screensaver.properties
file into your resources/ directory so that its runtime properties are 
available when running Screensaver tests or launching applications from 
within Eclipse.  

log4j.properties:

This file contains Log4J logging output options.  The screensaver.properties
file will specify the location of the log4j.properties file that is to be
installed at build-time via its 'log4j.properties.resource' property.
