BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20160922,
current_timestamp,
'add is_retired to funding_support';

alter table funding_support add column is_retired boolean default false;

COMMIT;