BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2456,
current_timestamp,
'changed status value';

UPDATE status_item set status_value = 'Pending - ICCB' where status_value = 'Pending';

COMMIT;
