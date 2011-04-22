#!/bin/bash
# runs a Screensaver command-line utility
# Found at: bin/io/run_expiration_services.sh
# Note: this version was created to run the expiration services in cron.
# usage: run.sh <fully qualifed class> <arg>...

ROOT_DIR=/groups/iccb/screensaver-orchestra/batch/
BSUB=/opt/lsf/6.0/linux2.6-glibc2.3-amd64/bin/bsub
JAVA_HOME=/usr/lib/jvm/java-6-sun

# Set up the LSF environment
. /opt/lsf/conf/profile.lsf

# Standard SS environment
SCREENSAVER=$ROOT_DIR/screensaver
JAVA=$JAVA_HOME/bin/java 
MAX_RAM_MB=1200
CLASSES=$SCREENSAVER/classes
JARS=`find $SCREENSAVER/lib -name "*.jar" -print`
LIBS=`for s in $JARS ; do printf ":$s" ; done`
CLASSPATH=$ROOT_DIR:$CLASSES:$LIBS
$BSUB -Is -q shared_int_2h $JAVA -Xmx${MAX_RAM_MB}m -cp $CLASSPATH "$@"
