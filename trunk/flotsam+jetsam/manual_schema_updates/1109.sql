ALTER TABLE screening_room_user DROP lab_affiliation_id;
DROP TABLE lab_affiliation;

    create table lab_affiliation (
        lab_affiliation_id varchar(2047) not null,
        version int4 not null,
        affiliation_name text not null unique,
        affiliation_category text not null,
        primary key (lab_affiliation_id)
    );

ALTER TABLE screening_room_user ADD lab_affiliation_id varchar(2047);

    alter table screening_room_user 
        add constraint fk_screening_room_user_to_lab_affiliation 
        foreign key (lab_affiliation_id) 
        references lab_affiliation;