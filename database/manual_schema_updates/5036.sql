BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5036,
current_timestamp,
'replace plate.date_plated with plate_update_activity record; add explicit plate.{retired,plated}_activity_id foreign key fields';

/* add explicit plate.retired_activity_id */

alter table plate add column retired_activity_id int4 unique;

update plate set retired_activity_id = (select max(update_activity_id) from plate_update_activity pua where pua.plate_id = plate.plate_id and plate.status = 'Retired');


/* replace plate.date_plated with plate_update_activity record; add explicit plate.plated_activity_id foreign key fields */

alter table plate add column plated_activity_id int4 unique;

create temp table plated_plate_activity (activity_id int, plate_id int, date_plated date);

insert into plated_plate_activity select nextval('activity_id_seq'), plate_id, p.date_plated from plate p where date_plated is not null;

/* TODO: update performed_by_id and created_by_id to an existing administrator_user */
insert into activity (activity_id, version, date_created, comments, date_of_activity, performed_by_id, created_by_id)
select ppa.activity_id, 0, now(), '''Plate Status Update'' activity created by database migration for plate''s ''Available'' status (the status has not been changed).', ppa.date_plated, 755, 755 
from plated_plate_activity ppa;

insert into administrative_activity (activity_id, administrative_activity_type) 
select activity_id, 'Plate Status Update' from plated_plate_activity;

insert into plate_update_activity (plate_id, update_activity_id) select plate_id, activity_id from plated_plate_activity;

update plate set status = 'Not specified' where status = 'Available' and date_plated is null;

update plate set plated_activity_id = (select max(update_activity_id) from plate_update_activity pua where pua.plate_id = plate.plate_id and plate.status = 'Available');

alter table plate drop column date_plated;

COMMIT;
