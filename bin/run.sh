# runs a Screensaver command-line utility
# usage: run.sh <fully qualifed class> <arg>...

SCREENSAVER=.
JAVA=$JAVA_HOME/bin/java 
MAX_RAM_MB=800
CLASSES=$SCREENSAVER/build/classes
JARS=`find $SCREENSAVER/lib \( -name unused -prune \) -or -name "*.jar" -print`
LIBS=`for s in $JARS ; do printf ":$s" ; done`
CLASSPATH=$CLASSES:$LIBS
$JAVA -Xmx${MAX_RAM_MB}m -cp $CLASSPATH "$@"

