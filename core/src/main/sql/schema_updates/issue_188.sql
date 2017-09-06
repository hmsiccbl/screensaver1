BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20170908,
current_timestamp,
'add appointment columns to lab_head';

alter table lab_head add column lab_head_appointment_category text;
alter table lab_head add column lab_head_appointment_department text;
alter table lab_head add column lab_head_appointment_update_date date;

COMMIT;