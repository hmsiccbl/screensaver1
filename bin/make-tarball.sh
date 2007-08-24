# builds a tarball of screensaver project for distribution purposes, excluding necessary files (svn, downloads, etc.)
# BROKEN, but almost there...
exit 1;
cd -P `dirname $0`/..
SCREENSAVER=`pwd`
SCREENSAVER=`basename $SCREENSAVER`
cd `dirname $0`/..
echo making tarball of \"$SCREENSAVER\" from `pwd`
THIS=$SCREENSAVER/bin/`basename $0`
tar czvf screensaver.tgz -follow --exclude '*compound-libraries*.zip' --exclude '*/.svn*' --exclude '*/lib/*/unused*' --exclude $THIS $SCREENSAVER
