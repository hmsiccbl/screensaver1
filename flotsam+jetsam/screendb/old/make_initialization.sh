# pseudo-script to record the sequence of steps used to migrate the
# screendb database into screensaver; end result is the creation of an
# SQL initialization script that will be run by SchemaUtil

echo You shouldn't actually be running this!
exit 1

dropdb -Upostgres screendb
createdb -Uscreendbweb screendb
psql screendb screendbweb < flotsam+jetsam/screendb/screendb-20070209.sql
ant clean
ant distro
rm resources/sql/initialize_database/01*sql
build/distro/screensaver/run.sh edu.harvard.med.screensaver.db.screendb.ScreenDBDataImporter
pg_dump --data-only --inserts -U devscreensaver1 devscreensaver1 > resources/sql/initialize_database/01_screendb_import_20070209.sql
# reorder inserts for tables, to respect relational constraints; 
# add/drop constraints as necessary (see previous version of file)