# run bin/orchestra/ss-build first!
#
# run this proggie something like this:
#
# bsub -q all_2h $SCREENSAVER/bin/io/compound_pubchem_cid_list_upgrader.sh
#
# TODO: improve placement of output and error files:
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
    edu.harvard.med.screensaver.io.libraries.compound.CompoundPubchemCidListUpgrader \
    -L ALL \
    > compound_pubchem_cid_list_upgrader.out \
    2> compound_pubchem_cid_list_upgrader.err

echo compound_pubchem_cid_list_upgrader.sh is complete
echo program output is in $SCREENSAVER_CHECKOUT/compound_pubchem_cid_list_upgrader.{out,err}
