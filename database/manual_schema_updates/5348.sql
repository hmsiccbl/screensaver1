BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5348,
current_timestamp,
'add copy.primary_plate_status';

alter table copy add column primary_plate_status text;
update copy set primary_plate_status = 
(select primary_plate_status from (select count(*) as cnt, p.status as primary_plate_status from plate p where p.copy_id = copy.copy_id group by p.status order by count(*) desc limit 1) as x);
alter table copy alter column primary_plate_status set not null;

COMMIT;
