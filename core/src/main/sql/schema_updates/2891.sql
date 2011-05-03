BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2891,
current_timestamp,
'add date_last_imported to screen_result';

ALTER TABLE screen_result ADD COLUMN date_last_imported TIMESTAMP; 
UPDATE screen_result SET date_last_imported = date_created;
ALTER TABLE screen_result ALTER COLUMN date_last_imported SET NOT NULL;

COMMIT;