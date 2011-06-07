BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5964,
current_timestamp,
'add attached file date';

alter table attached_file add column file_date date;

COMMIT;
