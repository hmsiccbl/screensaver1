BEGIN;

DROP SEQUENCE copy_id_seq;

DROP TABLE copy_action;
DROP TABLE copy_info;
DROP TABLE lab_cherry_pick;
DROP TABLE copy;

    create table copy (
        copy_id varchar(255) not null,
        version int4 not null,
        usage_type text,
        library_id int4 not null,
        name text not null,
        primary key (copy_id)
    );

    create table copy_info (
        copy_info_id int4 not null,
        version int4 not null,
        location text not null,
        plate_type text not null,
        volume numeric(19, 2) not null,
        comments text,
        date_plated timestamp,
        date_retired timestamp,
        copy_id varchar(255) not null,
        plate_number int4 not null,
        primary key (copy_info_id)
    );
    
    create table copy_action (
        copy_action_id int4 not null,
        version int4 not null,
        date timestamp not null,
        copy_info_id int4 not null,
        description text not null,
        primary key (copy_action_id)
    );
    
    create table lab_cherry_pick (
        lab_cherry_pick_id int4 not null,
        version int4 not null,
        cherry_pick_request_id int4 not null,
        screener_cherry_pick_id int4 not null,
        source_well_id varchar(255) not null,
        copy_id varchar(255),
        cherry_pick_assay_plate_id int4,
        assay_plate_row int4,
        assay_plate_column int4,
        primary key (lab_cherry_pick_id)
    );

    alter table copy 
        add constraint fk_copy_to_library 
        foreign key (library_id) 
        references library;

    alter table copy_action 
        add constraint fk_copy_action_to_copy_info 
        foreign key (copy_info_id) 
        references copy_info;

    alter table copy_info 
        add constraint fk_copy_info_to_copy 
        foreign key (copy_id) 
        references copy;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_screener_cherry_pick 
        foreign key (screener_cherry_pick_id) 
        references screener_cherry_pick;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_source_well 
        foreign key (source_well_id) 
        references well;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_copy 
        foreign key (copy_id) 
        references copy;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_cherry_pick_assay_plate 
        foreign key (cherry_pick_assay_plate_id) 
        references cherry_pick_assay_plate;

    alter table lab_cherry_pick 
        add constraint fk_cherry_pick_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

COMMIT;
