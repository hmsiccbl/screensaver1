BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3113,
current_timestamp,
'rename well.iccb_number to well.facility_id';

ALTER TABLE well RENAME iccb_number TO facility_id;

COMMIT;