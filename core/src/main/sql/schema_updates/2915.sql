BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2915,
current_timestamp,
'add date_created to library';

ALTER TABLE library ADD COLUMN date_created TIMESTAMP; 
UPDATE library SET date_created = '1970-01-01' WHERE date_created IS null;
ALTER TABLE library ALTER COLUMN date_created SET NOT NULL;
UPDATE library SET informatics_comments = informatics_comments || ', date_created set to 1970-01-01 by default' WHERE informatics_comments IS NOT NULL;
UPDATE library SET informatics_comments = 'date_created set to 1970-01-01 by default' WHERE informatics_comments IS NULL;
COMMIT;