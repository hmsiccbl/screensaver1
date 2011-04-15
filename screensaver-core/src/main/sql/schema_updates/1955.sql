BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1955,
current_timestamp,
'added various unique constraints';

alter table cherry_pick_assay_plate add constraint new_key unique (cherry_pick_request_id, plate_ordinal, attempt_ordinal);
alter table copy add constraint copy_key unique (library_id, name);
alter table copy_info add constraint copy_info_key unique (copy_id, plate_number);
alter table well_volume_adjustment add constraint well_volume_adjustment_key unique (copy_id, well_id, lab_cherry_pick_id, well_volume_correction_activity_id);
alter table annotation_type add constraint annotation_type_key unique (study_id, name);
alter table attached_file add constraint attached_file_key unique (screen_id, filename);
alter table status_item add constraint status_item_key unique (screen_id, status_date, status_value);
alter table checklist_item add constraint checklist_item_key unique (screening_room_user_id, checklist_item_type_id);

alter table screener_cherry_pick add constraint screener_cherry_pick_key unique (cherry_pick_request_id, screened_well_id);

COMMIT;