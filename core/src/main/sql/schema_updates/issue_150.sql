BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20141211,
current_timestamp,
'add user.gender controlled vocabulary';

alter table screensaver_user add column gender text;

COMMIT;
