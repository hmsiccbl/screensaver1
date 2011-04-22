BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5353,
current_timestamp,
'add copy.primary_plate_location';

alter table copy add column primary_plate_location_id int4;
update copy set primary_plate_location_id = 
(select primary_plate_location_id from (select count(*) as cnt, p.plate_location_id as primary_plate_location_id from plate p where p.copy_id = copy.copy_id group by p.plate_location_id order by count(*) desc limit 1) as x);

COMMIT;
