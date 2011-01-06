BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5121,
current_timestamp,
'rename copy usage type value';

update copy set usage_type = 'Stock Plates' where usage_type = 'Master Stock Plates';

COMMIT;
