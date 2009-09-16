# first, run 'ant -Dscreensaver.properties.file=<file> -Dinstall.dir install'
#
# usage:
# bsub $SCREENSAVER/bin/io/library_copy_generator.sh <args>
#
# usage (interactive/debug):
# bsub -Is -q shared_int_2h $SCREENSAVER/bin/io/library_copy_generator.sh <args>
#
# Configure database connection arguments in $SCREENSAVER/classes/screensaver.properties

SCREENSAVER=`dirname $0`/../..
cd $SCREENSAVER
SCREENSAVER=`pwd -P`
THIS=`basename $0 .sh`

LIBS=`for s in $SCREENSAVER/lib/*.jar ; do printf ":$s" ; done`
CLASSPATH="$SCREENSAVER/classes$LIBS"

java -Xmx1500m -cp $CLASSPATH \
    edu.harvard.med.screensaver.io.libraries.LibraryCopyGenerator "$@" \
    > $THIS.out 2> $THIS.err

echo $0 is complete
echo program output is in $SCREENSAVER/$THIS.{out,err}
