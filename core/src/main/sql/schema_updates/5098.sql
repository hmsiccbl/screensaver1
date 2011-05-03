BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5098,
current_timestamp,
'rename copy usage type value';

update copy set usage_type = 'Cherry Pick Source Plates' where usage_type = 'Cherry Pick Stock Plates';

COMMIT;
