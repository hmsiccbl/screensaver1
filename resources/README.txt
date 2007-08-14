A note regarding properties files:

The "screensaver.properties" file contains properties used at both build-time
(by Ant) and at run-time.  However, a file of this (exact) name does not
initially exist (it is not under version control).  For local deployment and 
for running programs out of Eclipse, soft-link from one of the
screensaver.properties.* files to screensaver.properties.  For deployment, you
can set the Ant property "screensaver.properties.file" to whatever file you
want to use, and the deployed system will use that value at runtime. (See
bin/orchestra-bin/ss-deploy, for example.)  You can also define any Ant
properties you want to in this file.

The "log4j.properties" file contains logging output options.  However, a file
of this (exact) name does not initially exist (it is not under version
control).  To affect the logging options of programs being run from Eclipse,
soft-link one of the log4j.properties.* files to log4j.properties.  For
deployment, you can set the "log4j.properties.resource" property in the
"screensaver.properties" file to the desired log4j.properties.* file, and the
specified file will be copied during deployment.
