This directory contains hand-written SQL "schema migration" files that when 
applied to a database will bring the existing database schema up-to-date with 
the latest Hibernate data model. These files are named after the SVN revision 
number they apply to. The expectation here is that these SQL files will be run 
by hand, when necessary, to bring an existing database up-to-date with the 
latest revision of Screensaver.

If this is the first time you are performing a schema migration, you must 
first run initialize_schema_history.sql.  This creates the table that 
maintains the history of schema migration operations that have been performed.

WARNING: These migration files are not guaranteed to preserve existing data!  
Carefully study each file and its effects on your database before applying it!

WARNING: Make sure you run these database updates as the appropriate user! 
(the same postgres user that your Tomcat deployment of Screensaver is 
configured to use.)  Otherwise you will run into permissions problems.

NOTE: When creating a new schema migration file, you must have it update the 
schema_history table by including the below SQL statement.  Replace "####" with 
the current screensaver revision number, and insert an appropriate comment that 
briefly chronicles the schema change.  The screensaver_revision value indicates 
the minimum Subversion-assigned revision number of the Screensaver project that 
will work with the current schema.

  INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
  SELECT ####, current_timestamp, '<your comment here>';

