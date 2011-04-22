BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2422,
current_timestamp,
'rm screen.is_downloadable';

ALTER TABLE screen DROP is_downloadable;

COMMIT;
