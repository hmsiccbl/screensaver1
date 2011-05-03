BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4007,
current_timestamp,
'rename copy_info to plate; drop unused copy_action table';

alter table copy_info rename to plate;
alter table plate rename copy_info_id to plate_id;
alter sequence copy_info_id_seq rename to plate_id_seq ;
alter table plate drop constraint fk_copy_info_to_copy;
alter table plate add constraint fk_plate_to_copy foreign key (copy_id) references copy;
alter index copy_info_pkey rename to plate_pkey;
alter index copy_info_key rename to plate_key;

/* TODO: copy_action is no longer supported or used by Screensaver and
should be empty; you may want to verify this before dropping it */
drop table copy_action;



COMMIT;
