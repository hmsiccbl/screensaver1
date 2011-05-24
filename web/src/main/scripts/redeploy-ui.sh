#!/bin/sh
# quick & dirty mechanism for hot deploying UI files to a running Tomcat instance
UI_DIR=${1:-./web/src/main/webapp/}
WEBAPP=${2:-/usr/local/tomcat/webapps/screensaver/}
# prevent the war contents from overwriting our replacement of exploded files
rm $WEBAPP/../screensaver*.war
CMD="rsync -av --exclude=*.svn* --exclude=*-INF* $UI_DIR $WEBAPP"
echo $CMD
$CMD
