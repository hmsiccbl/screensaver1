BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5545,
current_timestamp,
'add copy.plate_locations_count';

alter table copy add column plate_locations_count int4;
update copy set plate_locations_count = (select count(distinct(plate_location_id)) from plate p where  p.copy_id = copy.copy_id);

COMMIT;
