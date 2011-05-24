BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4448,
current_timestamp,
'add quality_control_information to library';

alter table library add column quality_control_information text;

COMMIT;