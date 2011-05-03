begin;

alter table rnai_knockdown_confirmation 
    drop constraint fk_rnai_knockdown_confirmation_to_cherry_pick;

alter table cherry_pick_liquid_transfer 
    drop constraint fk_cherry_pick_liquid_transfer_to_cherry_pick_request;

alter table cherry_pick_liquid_transfer_cherry_pick_link 
    drop constraint FK1A47CCFBAFA809F0 ;

drop table cherry_pick;

create table cherry_pick_assay_plate (
    cherry_pick_assay_plate_id int4 not null,
    version int4 not null,
    cherry_pick_request_id int4 not null,
    plate_ordinal int4 not null,
    attempt_ordinal int4 not null,
    comments text,
    cherry_pick_liquid_transfer_id int4,
    assay_plate_type text,
    primary key (cherry_pick_assay_plate_id)
);

alter table cherry_pick_liquid_transfer drop column cherry_pick_request_id;

drop table cherry_pick_liquid_transfer_cherry_pick_link;

create table lab_cherry_pick (
    lab_cherry_pick_id int4 not null,
    version int4 not null,
    cherry_pick_request_id int4 not null,
    screener_cherry_pick_id int4 not null,
    source_well_id varchar(255) not null,
    copy_id int4,
    cherry_pick_assay_plate_id int4,
    assay_plate_row int4,
    assay_plate_column int4,
    primary key (lab_cherry_pick_id)
);

create table screener_cherry_pick (
    screener_cherry_pick_id int4 not null,
    version int4 not null,
    cherry_pick_request_id int4 not null,
    screened_well_id varchar(255) not null,
    is_hit_confirmed_via_experimentation text,
    notes_on_hit_confirmation text,
    primary key (screener_cherry_pick_id)
);

alter table rnai_knockdown_confirmation rename column cherry_pick_id to screener_cherry_pick_id;

alter table cherry_pick_assay_plate 
    add constraint fk_cherry_pick_assay_plate_to_cherry_pick_liquid_transfer 
    foreign key (cherry_pick_liquid_transfer_id) 
    references cherry_pick_liquid_transfer;

alter table cherry_pick_assay_plate 
    add constraint fk_cherry_pick_assay_plate_to_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

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

alter table rnai_knockdown_confirmation 
    add constraint fk_rnai_knockdown_confirmation_to_screener_cherry_pick 
    foreign key (screener_cherry_pick_id) 
    references screener_cherry_pick;

alter table screener_cherry_pick 
    add constraint fk_screener_cherry_pick_to_screened_well 
    foreign key (screened_well_id) 
    references well;

alter table screener_cherry_pick 
    add constraint fk_screener_cherry_pick_to_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

create sequence cherry_pick_assay_plate_id_seq;

create sequence screener_cherry_pick_id_seq;

create sequence lab_cherry_pick_id_seq;

commit;