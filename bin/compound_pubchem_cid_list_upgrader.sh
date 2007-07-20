# run bin/orchestra/ss-build first!
#
# run this proggie something like this:
#
# bsub -q all_2h /home/js163/screensaver/bin/compound_pubchem_cid_list_upgrader.sh
#
# TODO: improve placement of output and error files:
#   - put a timestamp or something in the filenames so they dont overwrite
#   - check for existence of files before overwriting?

SCREENSAVER_CHECKOUT=$HOME/screensaver
BUILD=$SCREENSAVER_CHECKOUT/build

cd $SCREENSAVER_CHECKOUT

LIBS=`for s in $BUILD/lib/*.jar ; do printf ":$s" ; done`
CLASSPATH="$BUILD/classes$LIBS"
JAVA=/opt/java/jdk1.5/bin/java

$JAVA -Xmx1500m -cp $CLASSPATH \
    edu.harvard.med.screensaver.io.libraries.compound.CompoundPubchemCidListUpgrader \
    -L ALL \
    > compound_pubchem_cid_list_upgrader.out \
    2> compound_pubchem_cid_list_upgrader.err

echo compound_pubchem_cid_list_upgrader.sh is complete
echo program output is in $SCREENSAVER_CHECKOUT/compound_pubchem_cid_list_upgrader.{out,err}

