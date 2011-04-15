DB_NAME=$1
DB_USER=$2
PRJ_DIR=`dirname $0`/../..
CREATE_SCHEMA_SQL=$PRJ_DIR/build/ddl/create_schema.sql
dropdb $DB_NAME -U $DB_USER
createdb $DB_NAME -U $DB_USER
(cd $PRJ_DIR && ant ddl -Dscreensaver.properties.file=cfg/screensaver.properties.test)
psql $DB_NAME $DB_USER -f $CREATE_SCHEMA_SQL
