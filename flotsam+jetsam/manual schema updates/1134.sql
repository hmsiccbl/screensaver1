begin;


/* begin, temporary drop of constraints */

alter table rnai_knockdown_confirmation 
    drop constraint fk_rnai_knockdown_confirmation_to_cherry_pick ;

/* end, temporary drop of constraints */


drop table cherry_pick;

create table cherry_pick (
    cherry_pick_id int4 not null,
    version int4 not null,
    cherry_pick_request_id int4 not null,
    well_id varchar(255) not null,
    copy_id int4 not null,
    cherry_pick_liquid_transfer_id int4,
    is_hit_confirmed_via_experimentation text,
    notes_on_hit_confirmation text,
    destination_plate_type text,
    destination_plate_name text,
    primary key (cherry_pick_id)
);

create table cherry_pick_liquid_transfer (
    screening_room_activity_id int4 not null,
    cherry_pick_request_id int4 not null,
    actual_microliter_transfer_volume_per_well numeric(19, 2),
    primary key (screening_room_activity_id)
);

create table cherry_pick_request (
    cherry_pick_request_id int4 not null,
    version int4 not null,
    screen_id int4 not null,
    date_requested timestamp not null,
    microliter_transfer_volume_per_well_requested numeric(19, 2),
    microliter_transfer_volume_per_well_approved numeric(19, 2),
    comments text,
    requested_by_id int4 not null,
    primary key (cherry_pick_request_id)
);

create table compound_cherry_pick_request (
    cherry_pick_request_id int4 not null,
    primary key (cherry_pick_request_id)
);

alter table copy add column usage_type text;

alter table equipment_used drop column visit_id;

alter table equipment_used add column screening_room_activity_id int4 not null;

create table legacy_screening_room_activity (
    screening_room_activity_user_id int4 not null,
    legacy_activity_type_name text,
    primary key (screening_room_activity_user_id)
);

create table library_screening (
    screening_room_activity_id int4 not null,
    volume_of_compound_transferred text,
    assay_protocol_type text,
    primary key (screening_room_activity_id)
);

alter table plates_used drop column visit_id;

alter table plates_used add column library_screening_id int4 not null;

create table rnai_cherry_pick_request (
    cherry_pick_request_id int4 not null,
    assay_protocol varchar(255),
    primary key (cherry_pick_request_id)
);

create table rnai_cherry_pick_screening (
    screening_room_activity_id int4 not null,
    cherry_pick_request_id int4 not null,
    primary key (screening_room_activity_id)
);

create table screening (
    screening_room_activity_user_id int4 not null,
    assay_protocol text,
    number_of_replicates int4,
    primary key (screening_room_activity_user_id)
);

create table screening_room_activity (
    screening_room_activity_id int4 not null,
    version int4 not null,
    screen_id int4 not null,
    date timestamp not null,
    comments text,
    performed_by_id int4 not null,
    primary key (screening_room_activity_id)
);

drop table visit;

alter table cherry_pick 
    add constraint fk_cherry_pick_to_cherry_pick_liquid_transfer 
    foreign key (cherry_pick_liquid_transfer_id) 
    references cherry_pick_liquid_transfer;

alter table cherry_pick 
    add constraint fk_cherry_pick_to_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

alter table cherry_pick_liquid_transfer 
    add constraint FKE177A656858D850D 
    foreign key (screening_room_activity_id) 
    references screening_room_activity;

alter table cherry_pick_liquid_transfer 
    add constraint fk_cherry_pick_liquid_transfer_to_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

alter table cherry_pick_request 
    add constraint fk_cherry_pick_request_to_screen 
    foreign key (screen_id) 
    references screen;

alter table cherry_pick_request 
    add constraint fk_cherry_pick_request_to_requested_by 
    foreign key (requested_by_id) 
    references screening_room_user;

alter table compound_cherry_pick_request 
    add constraint FK15F250C38E09CC35 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

alter table equipment_used 
    add constraint fk_equipment_used_to_screening_room_activity 
    foreign key (screening_room_activity_id) 
    references screening_room_activity;

alter table legacy_screening_room_activity 
    add constraint FK97EE72F489799937 
    foreign key (screening_room_activity_user_id) 
    references screening_room_activity;

alter table library_screening 
    add constraint FK62CFA172ECB24863 
    foreign key (screening_room_activity_id) 
    references screening;

alter table plates_used 
    add constraint fk_plates_used_to_library_screening 
    foreign key (library_screening_id) 
    references library_screening;

alter table rnai_cherry_pick_request 
    add constraint FKD327EBFC8E09CC35 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

alter table rnai_cherry_pick_screening 
    add constraint FK86F54223ECB24863 
    foreign key (screening_room_activity_id) 
    references screening;

alter table rnai_cherry_pick_screening 
    add constraint fk_rnai_cherry_pick_screening_to_rnai_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references rnai_cherry_pick_request;

alter table screening 
    add constraint FK774EFF689799937 
    foreign key (screening_room_activity_user_id) 
    references screening_room_activity;

alter table screening_room_activity 
    add constraint fk_screening_room_activity_to_screen 
    foreign key (screen_id) 
    references screen;

alter table screening_room_activity 
    add constraint fk_screening_room_activity_to_performed_by 
    foreign key (performed_by_id) 
    references screening_room_user;

create sequence cherry_pick_request_id_seq;

create sequence screening_room_activity_id_seq;

drop sequence visit_id_seq;


/* begin, recreate temporarily dropped constraints */

alter table rnai_knockdown_confirmation 
    add constraint fk_rnai_knockdown_confirmation_to_cherry_pick 
    foreign key (cherry_pick_id) 
    references cherry_pick;

/* end, recreate temporarily dropped constraints */


commit;