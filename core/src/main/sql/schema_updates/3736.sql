BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3736,
current_timestamp,
'drop library_contents_version.date_created';

alter table library_contents_version drop column date_created;

COMMIT;