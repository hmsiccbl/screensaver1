# run bin/orchestra/ss-build first!
#
# usage:
# bsub ~/screensaver/bin/import_screen_result.sh <screen#>
#
# usage (interactive/debug):
# bsub -Is -q shared_int_2h ~/screensaver/bin/import_screen_result.sh <screen#>
#
# Screen result file must be $SCREEN_RESULTS/<screen#>_finalResults.xls
#
# Configure database connection arguments in $SCREENSAVER/classes/screensaver.properties

SCREENSAVER_CHECKOUT=$HOME/screensaver
SCREENSAVER=$SCREENSAVER_CHECKOUT/build/distro/screensaver
SCREEN_RESULTS=${SCREEN_RESULTS:-$HOME/screen-results}

cd $SCREENSAVER

LIBS=`for s in lib/*.jar ; do printf ":$s" ; done`
CLASSPATH="$SCREENSAVER/classes$LIBS"
JAVA=/opt/java/jdk1.5/bin/java

#echo running from `pwd`
#echo classpath=$CLASSPATH
$JAVA -Xmx1500m -cp $CLASSPATH edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter -f $SCREEN_RESULTS/$1_finalResults.xls -s $1 -w 4 -i
echo $0 terminated
