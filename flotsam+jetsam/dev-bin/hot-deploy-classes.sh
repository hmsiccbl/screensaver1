CLASSES_DIR=${1:-/usr/local/tomcat/webapps/screensaver/WEB-INF/classes/}
rsync -a .eclipse.classes/edu $CLASSES_DIR
#wget --user admin --password @dmin http://localhost:8080/manager/html/reload?path=/screensaver > /dev/null 2>&1 &
touch $CLASSES_DIR/../web.xml