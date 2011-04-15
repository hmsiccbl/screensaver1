BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2758,
current_timestamp,
'status value "Piloted" -> "Piloting"';

UPDATE status_item set status_value = 'Piloting' where status_value = 'Piloted';

COMMIT;