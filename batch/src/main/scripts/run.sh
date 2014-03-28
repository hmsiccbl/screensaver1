#!/bin/bash
# runs a Screensaver command-line utility
# usage: run.sh <fully qualified class> <arg>...

if [[ $1 == '-debug' ]]; then
  echo Running in debug mode...
  DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000
  shift 1
fi

SCREENSAVER=.
JAVA=$JAVA_HOME/bin/java 
MAX_RAM=2000m
RESOURCES=$SCREENSAVER/resources
JARS=`find $SCREENSAVER/lib -name "*.jar" -print`
LIBS=`for s in $JARS ; do printf ":$s" ; done`
CLASSPATH=.:$RESOURCES:$LIBS
$JAVA -Dscreensaver.properties.file="${SCREENSAVER_PROPERTIES_FILE}" -Xmx${MAX_RAM} $DEBUG_OPTIONS -cp $CLASSPATH "$@"
