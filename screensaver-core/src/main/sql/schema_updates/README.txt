This directory contains hand-written SQL "schema migration" files that when 
applied to a database will bring the existing database schema up-to-date with 
the latest Screensaver data model. These files are named after the SVN revision 
number they apply to. The expectation here is that these SQL files will be run 
by hand, when necessary, to bring an existing database up-to-date with the 
latest revision of Screensaver.

For instructions on how to apply these scripts, see 
https://wiki.med.harvard.edu/ICCBL/Screensaver/ScreensaverAdministrationGuide#Database_Migration.

---

For Developers: 

When creating a new schema migration script, you must have it update the 
schema_history table by including the below SQL statement.  Replace "####" with 
the current screensaver revision number, and insert an appropriate comment that 
briefly chronicles the schema change.  The screensaver_revision value indicates 
the minimum Subversion-assigned revision number of the Screensaver project that 
will work with the current schema.

  INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
  SELECT ####, current_timestamp, '<your comment here>';
  
All scripts should start with a BEGIN SQL command and end with COMMIT.  
This ensures that errors encountered during the update will not leave the 
database in an inconsistent state.   

