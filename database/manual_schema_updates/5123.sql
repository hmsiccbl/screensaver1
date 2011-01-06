BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5123,
current_timestamp,
'reset status of all plates to "Not specified"';

/* TODO: For ICCB-L, which is using PlateBatchUpdater to set the status of all existing plates, and so we want all plates' status to be reset before this batch update is performed. */

create temp table reset_plate_status_activity (activity_id int, plate_id int);

insert into reset_plate_status_activity select nextval('activity_id_seq'), plate_id from plate p;

/* TODO: update performed_by_id and created_by_id to an existing administrator_user */
insert into activity (activity_id, version, date_created, comments, date_of_activity, performed_by_id, created_by_id)
select rpsa.activity_id, 0, now(), '''Plate Status Update'' activity created by database migration to reset plate''s status to ''Not specified''.', now(), 755, 755 
from reset_plate_status_activity rpsa;

insert into administrative_activity (activity_id, administrative_activity_type) 
select activity_id, 'Plate Status Update' from reset_plate_status_activity;

insert into plate_update_activity (plate_id, update_activity_id) select plate_id, activity_id from reset_plate_status_activity;

update plate set plated_activity_id = null;
update plate set retired_activity_id = null;
update plate set status = 'Not specified';

COMMIT;
