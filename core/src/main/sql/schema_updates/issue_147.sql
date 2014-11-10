BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20141107,
current_timestamp,
'add screen.assayType controlled vocabulary';

alter table screen add column assay_type text;

COMMIT;
