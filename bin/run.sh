# runs a Screensaver command-line utility
# usage: run.sh <fully qualifed class> <arg>...

$SCREENSAVER=.
$JAVA=$JAVA_HOME/bin/java 
$MAX_RAM_MB=800
$CLASSES=$SCREENSAVER/classes
$LIBS=`find $SCREENSAVER/lib -name '*.jar' -printf ':%p'`
$CLASSPATH=$CLASSES:$LIB
export CLASSPATH=classes:$LIBS
$JAVA -Xmx$MAX_RAM_MBm -cp $CLASSPATH "$@"

