BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3040,
current_timestamp,
'add screen COMS fields';

ALTER TABLE screen ADD COLUMN coms_registration_number text;
ALTER TABLE screen ADD COLUMN coms_approval_date date;

COMMIT;