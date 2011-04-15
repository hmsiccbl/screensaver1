BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2835,
current_timestamp,
'add concentration field to lab_activity';

ALTER TABLE lab_activity ADD COLUMN concentration numeric(12, 12);

COMMIT;