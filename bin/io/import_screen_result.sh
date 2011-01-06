# ICCB-L-specific script for running the cmd-line ScreenResultImporter 
# 
# To install:
# $ ant -Dscreensaver.properties.file=<file> -Dinstall.dir=<dir> install
# Configure database connection arguments in $SCREENSAVER/classes/screensaver.properties
#
# To run:
# bsub $SCREENSAVER/bin/io/import_screen_result.sh <screen#> <admin eCommons ID> [<start plate #> <end plate #> [<'append'>]]
#
# Screen result file must be $SCREEN_RESULTS/<screen#>_finalResults.xls

SCREENSAVER=`dirname $0`/../..
cd $SCREENSAVER
SCREENSAVER=`pwd -P`

LIBS=`for s in $SCREENSAVER/lib/*.jar ; do printf ":$s" ; done`
CLASSPATH="$SCREENSAVER/classes$LIBS"

SCREEN_NUMBER=$1
ADMIN_ECOMMONS_ID=$2
SCREEN_RESULTS=${SCREEN_RESULTS:-$HOME/screen-results}
START_PLATE=$2
END_PLATE=$3
APPEND=$4

java -Xmx1500m -cp $CLASSPATH \
    edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
    -AE $2
    -f $SCREEN_RESULTS/${SCREEN_NUMBER}_finalResults.xls -s ${SCREEN_NUMBER} -i \
    ${START_PLATE:+ -sp $START_PLATE} ${END_PLATE:+ -ep $END_PLATE} ${APPEND:+ --append} \
    > import_screen_result.out \
    2> import_screen_result.err

echo $0 is complete
echo program output is in $SCREENSAVER/import_screen_result.{out,err}
