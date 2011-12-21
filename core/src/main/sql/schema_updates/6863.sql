BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6863,
current_timestamp,
'add sru.last_notified_rnaiua_checklist_item_event_id';

alter table screening_room_user add column last_notified_rnaiua_checklist_item_event_id int4;

alter table screening_room_user 
    add constraint fk_screening_room_user_to_notified_rnaiua_checklist_item_event 
    foreign key (last_notified_rnaiua_checklist_item_event_id) 
    references checklist_item_event;

/* note: 3805.sql did not add foreign key constraint for last_notified_smua_checklist_item_event_id, adding it now */
alter table screening_room_user 
    add constraint fk_screening_room_user_to_notified_smua_checklist_item_event 
    foreign key (last_notified_smua_checklist_item_event_id) 
    references checklist_item_event;


COMMIT;