BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3549,
current_timestamp,
'administrative_activity changes, entity update tables (audit logs)';



alter table activity add created_by_id int4;
create table activity_update_activity (
    activity_id int4 not null,
    update_activity_id int4 not null,
    primary key (activity_id, update_activity_id)
);
alter table activity 
    add constraint FK9D4BF30F66AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table activity_update_activity 
    add constraint FKCE1507F3ACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table activity_update_activity 
    add constraint FKCE1507F3702E36D6 
    foreign key (activity_id) 
    references activity;

alter table attached_file add created_by_id int4;
create table attached_file_update_activity (
    attached_file_id int4 not null,
    update_activity_id int4 not null,
    primary key (attached_file_id, update_activity_id)
);
alter table attached_file 
    add constraint FKFC6F173766AB751E 
    foreign key (created_by_id) 
    references screensaver_user;

alter table attached_file_update_activity 
    add constraint FKE2D6721BACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;

alter table attached_file_update_activity 
    add constraint FKE2D6721BD73F25DF 
    foreign key (attached_file_id) 
    references attached_file;


alter table checklist_item_event add date_created timestamp;
alter table checklist_item_event add created_by_id int4;
create table checklist_item_event_update_activity (
    checklist_item_event_id int4 not null,
    update_activity_id int4 not null,
    primary key (checklist_item_event_id, update_activity_id)
);
alter table checklist_item_event 
    add constraint FK21A896A766AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table checklist_item_event_update_activity 
    add constraint FKCD8F58BACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table checklist_item_event_update_activity 
    add constraint FKCD8F58B84DCB316 
    foreign key (checklist_item_event_id) 
    references checklist_item_event;

alter table library add created_by_id int4;
create table library_update_activity (
    library_id int4 not null,
    update_activity_id int4 not null,
    primary key (library_id, update_activity_id)
);
alter table library 
    add constraint FK9E824BB66AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table library_update_activity 
    add constraint FK96E0A69FACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table library_update_activity 
    add constraint FK96E0A69F1E25BD89 
    foreign key (library_id) 
    references library;


alter table screen add created_by_id int4;
create table screen_update_activity (
    screen_id int4 not null,
    update_activity_id int4 not null,
    primary key (screen_id, update_activity_id)
);
alter table screen 
    add constraint FKC9E5C06C66AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table screen_update_activity 
    add constraint FK8D9B1810ACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table screen_update_activity 
    add constraint FK8D9B1810806C52FD 
    foreign key (screen_id) 
    references screen;

alter table screen_result add created_by_id int4;
create table screen_result_update_activity (
    screen_result_id int4 not null,
    update_activity_id int4 not null,
    primary key (screen_result_id, update_activity_id)
);
alter table screen_result 
    add constraint FK58DEF35066AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table screen_result_update_activity 
    add constraint FK232AD9F4ACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table screen_result_update_activity 
    add constraint FK232AD9F4852975F3 
    foreign key (screen_result_id) 
    references screen_result;

alter table screensaver_user add created_by_id int4;
create table screensaver_user_update_activity (
    screensaver_user_id int4 not null,
    update_activity_id int4 not null,
    primary key (screensaver_user_id, update_activity_id)
);
alter table screensaver_user 
    add constraint FK39B734A166AB751E 
    foreign key (created_by_id) 
    references screensaver_user;
alter table screensaver_user_update_activity 
    add constraint FKD3BDC905ACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table screensaver_user_update_activity 
    add constraint FKD3BDC905612ADCAB 
    foreign key (screensaver_user_id) 
    references screensaver_user;

update checklist_item_event set date_created = a.date_created, created_by_id = a.performed_by_id from activity a where a.activity_id = checklist_item_event.entry_admin_activity_id;
alter table checklist_item_event alter date_created set not null;
alter table checklist_item_event drop constraint fk_well_to_entry_admin_activity;
delete from administrative_activity where activity_id in (select entry_admin_activity_id from checklist_item_event);
delete from activity where activity_id in (select entry_admin_activity_id from checklist_item_event);
alter table checklist_item_event drop column entry_admin_activity_id;

update activity set created_by_id = performed_by_id from administrative_activity aa where aa.activity_id = activity.activity_id and aa.approved_by_id is not null;
update activity set performed_by_id = approved_by_id from administrative_activity aa where aa.activity_id = activity.activity_id and aa.approved_by_id is not null;
alter table administrative_activity drop column date_approved;
alter table administrative_activity drop column approved_by_id;

COMMIT;
