BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4995,
current_timestamp,
'replace plate.date_retired and plate.comments with plate_update_activity record';

/* TODO: verify that plate.comments is only set when plate.date_retired is not null, since comments will only be saved for retired plates; all other comments will be dropped! */

create temp table retired_plate_activity (activity_id int, plate_id int, date_retired date, comments text);

insert into retired_plate_activity select nextval('activity_id_seq'), plate_id, p.date_retired, p.comments from plate p where date_retired is not null;

/* TODO: update performed_by_id and created_by_id to an existing administrator_user */
insert into activity (activity_id, version, date_created, comments, date_of_activity, performed_by_id, created_by_id)
select rpa.activity_id, 0, now(), 'database migration set status to ''Retired'' (activity ''performed by'' is unknown). ' || coalesce(rpa.comments, ''), rpa.date_retired, 755, 755 
from retired_plate_activity rpa;

insert into administrative_activity (activity_id, administrative_activity_type) 
select activity_id, 'Plate Status Update' from retired_plate_activity;

insert into plate_update_activity (plate_id, update_activity_id) select plate_id, activity_id from retired_plate_activity;

update plate set status = 'Retired' where date_retired is not null;

alter table plate drop column date_retired; 
alter table plate drop column comments;

COMMIT;
