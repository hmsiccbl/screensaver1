BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2504,
current_timestamp,
'added pending-legacy status value';

UPDATE status_item set status_value = 'Pending - Legacy' where status_value = 'Pending - ICCB';

COMMIT;
