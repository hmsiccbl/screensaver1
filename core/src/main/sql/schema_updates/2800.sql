BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2800,
current_timestamp,
'add the comment field to screen_result';

ALTER table screen_result ADD COLUMN comments TEXT;

COMMIT;