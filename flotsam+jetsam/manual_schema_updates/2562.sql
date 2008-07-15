BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2562,
current_timestamp,
'ChecklistItem changes';

DROP TABLE checklist_item CASCADE;
DROP TABLE checklist_item_type CASCADE;
DROP SEQUENCE checklist_item_id_seq;
DROP SEQUENCE checklist_item_type_id_seq;

create table checklist_item (
    checklist_item_id int4 not null,
    is_expirable bool not null,
    item_name text not null unique,
    order_statistic int4 not null unique,
    version int4 not null,
    primary key (checklist_item_id)
);

create table checklist_item_event (
    checklist_item_event_id int4 not null,
    date_performed date,
    is_expiration bool not null,
    version int4 not null,
    checklist_item_id int4 not null,
    entry_admin_activity_id int4 not null,
    screening_room_user_id int4 not null,
    primary key (checklist_item_event_id)
);

alter table checklist_item_event 
    add constraint fk_checklist_item_event_to_checklist_item 
    foreign key (checklist_item_id) 
    references checklist_item;

alter table checklist_item_event 
    add constraint fk_well_to_entry_admin_activity 
    foreign key (entry_admin_activity_id) 
    references administrative_activity;

alter table checklist_item_event 
    add constraint fk_checklist_item_event_to_screening_room_user 
    foreign key (screening_room_user_id) 
    references screening_room_user;


create sequence checklist_item_id_seq;

create sequence checklist_item_event_id_seq;


COMMIT;
