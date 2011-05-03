BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2228,
current_timestamp,
'add screensaver_user.harvard_id_expiration_date';

ALTER TABLE screensaver_user ADD COLUMN harvard_id_expiration_date TIMESTAMP;

COMMIT;
