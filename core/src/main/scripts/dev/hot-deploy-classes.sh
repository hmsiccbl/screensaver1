# quick & dirty mechanism for hot deploying class files to a running Tomcat instance
CLASSES_DIR=${1:-/usr/local/tomcat/webapps/screensaver/WEB-INF/classes/}
rsync -a .eclipse.classes/edu $CLASSES_DIR
touch $CLASSES_DIR/../web.xml