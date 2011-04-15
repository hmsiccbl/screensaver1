BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2799,
current_timestamp,
'library.screening_status, plates_used.copy_name';

alter table library rename column is_screenable to screening_status;
update library set screening_status = 'Allowed' where screening_status is null;
alter table library alter column screening_status set not null;

alter table plates_used rename column copy to copy_name;

COMMIT;