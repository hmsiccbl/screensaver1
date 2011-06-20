DB_NAME=$1
DB_USER=$2
PRJ_DIR=`dirname $0`/../../../..
CREATE_SCHEMA_SQL=$PRJ_DIR/target/hibernate3/sql/screensaver_schema.sql
dropdb $DB_NAME -U $DB_USER
createdb $DB_NAME -U $DB_USER
(cd $PRJ_DIR && mvn org.codehaus.mojo:hibernate3-maven-plugin:2.2:hbm2ddl)
psql $DB_NAME $DB_USER -f $CREATE_SCHEMA_SQL
