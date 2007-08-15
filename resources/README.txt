A note regarding properties files:

The "screensaver.properties" file contains properties used at both build-time
(by Ant) and at run-time.  However, a file of this (exact) name does not
initially exist (i.e., it is not under version control).  Instead, variations
of this file exist, allowing developers to maintain different build-time and
run-time configurations.  For local web deployment and for running programs
out of Eclipse, you can either copy and modify one of the existing
screensaver.properties.* files, or soft-link one of these to
resources/screensaver.properties.  For local web deployment, you can also
(alternately) specify the Ant property "screensaver.properties.file" to point
to the file you want to use, and the deployed system will use that value at
runtime (e.g., see bin/orchestra-bin/ss-deploy).  You can also define any Ant
properties you want to in this file.

The "log4j.properties" file contains logging output options.  However, a file
of this (exact) name does not initially exist (i.e., it is not under version
control).  Instead, variations of this file exist, allowing developers to
maintain various logging configurations for debugging, production deployment,
etc.  The screensaver.properties file will specify which log4j.properties.*
file is to be installed at build-time via its "log4j.properties.resource"
property.  However, when running programs from an IDE, it can be useful to
configure the logging options, say, for debugging.  To affect the logging
options of programs being run from your IDE, symlink one of the
log4j.properties.* files to log4j.properties (log4j.properties.debug is
probably a good choice).
