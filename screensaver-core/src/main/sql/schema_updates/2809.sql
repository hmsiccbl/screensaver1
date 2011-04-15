BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2809,
current_timestamp,
'add the harvard_id_requested_expiration_date field to screensaver_user';

ALTER table screensaver_user ADD COLUMN harvard_id_requested_expiration_date DATE;

COMMIT;