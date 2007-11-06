# run bin/orchestra/ss-build first!
#
# run this proggie something like this:
#
# bsub $SCREENSAVER/bin/io/screendbsynchronizer.sh
#
# TODO: improve placement of output and error files:
#   - some place secure?
#   - put a timestamp or something in the filenames so they dont overwrite
#   - check for existence of files before overwriting?
#
# Configure database connection arguments in $SCREENSAVER/classes/screensaver.properties

SCREENSAVER=`dirname $0`/../..
cd $SCREENSAVER
SCREENSAVER=`pwd -P`

LIBS=`for s in $SCREENSAVER/lib/*.jar ; do printf ":$s" ; done`
CLASSPATH="$SCREENSAVER/classes$LIBS"
JAVA=/opt/java/jdk1.5/bin/java

$JAVA -Xmx1500m -cp $CLASSPATH \
    edu.harvard.med.screensaver.db.screendb.OrchestraStandaloneScreenDBSynchronizer \ 
    -S pgsql.cl.med.harvard.edu -D screendb -U $USER \ 
    > screendb_synchronizer.out \
    2> screendb_synchronizer.err

echo screendb_synchronizer.sh is complete
echo program output is in $SCREENSAVER/screendb_synchronizer.{out,err}

