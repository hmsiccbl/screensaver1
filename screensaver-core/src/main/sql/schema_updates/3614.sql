BEGIN;

INSERT INTO schema_history(screensaver_revision, date_updated, comment)
SELECT
3614,
current_timestamp,
'migrate data sharing level info for screens and users';


/* Note: this was an ICCB-L-specific migration script, whose contents have been deleted.
   The file is migration script file is being maintained to simplify future branch/merge management. */

COMMIT;


