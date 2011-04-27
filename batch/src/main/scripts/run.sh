# runs a Screensaver command-line utility
# usage: run.sh <fully qualifed class> <arg>...

while getopts 'd' opt; do
  echo Running in debug mode...
  DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000
done
shift $(( $OPTIND - 1 ))

SCREENSAVER=.
JAVA=$JAVA_HOME/bin/java 
MAX_RAM=800m
RESOURCES=$SCREENSAVER/resources
JARS=`find $SCREENSAVER/lib -name "*.jar" -print`
LIBS=`for s in $JARS ; do printf ":$s" ; done`
CLASSPATH=.:$RESOURCES:$LIBS
$JAVA -Xmx${MAX_RAM} $DEBUG_OPTIONS -cp $CLASSPATH "$@"
