ROOT=`dirname $0`/../../build/distro/screensaver
CLASSPATH=$ROOT/classes:`find $ROOT -follow -name '*.jar' -type f -printf "%h/%f:"`
SCREENSAVER_PGSQL_SERVER=localhost
SCREENSAVER_PGSQL_DB=testscreensaver1
SCREENSAVER_PGSQL_USER=testscreensaver1
java -cp $CLASSPATH edu.harvard.med.screensaver.db.HibernateConsole --dbhost $SCREENSAVER_PGSQL_SERVER --dbname $SCREENSAVER_PGSQL_DB --dbuser $SCREENSAVER_PGSQL_USER