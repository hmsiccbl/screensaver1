BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4663,
current_timestamp,
'add image_url field to study; note: this is a GO-only feature';

alter table screen add column image_url text;

COMMIT;
