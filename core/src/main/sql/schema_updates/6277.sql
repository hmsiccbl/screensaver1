BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6277,
current_timestamp,
'add service_activity table';

    create table service_activity (
        service_activity_type text not null,
        activity_id int4 not null,
        serviced_screen_id int4,
        serviced_user_id int4 not null,
        primary key (activity_id)
    );

    alter table service_activity 
        add constraint FKD39195944854A19 
        foreign key (serviced_user_id) 
        references screening_room_user;

    alter table service_activity 
        add constraint fk_service_activity_to_activity 
        foreign key (activity_id) 
        references activity;

    alter table service_activity 
        add constraint FKD3919593961870D 
        foreign key (serviced_screen_id) 
        references screen;

COMMIT;
