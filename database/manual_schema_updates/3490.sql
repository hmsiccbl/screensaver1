BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3490,
current_timestamp,
'add assay_well table';

create table assay_well (
    assay_well_id int4 not null,
    version int4 not null,
    assay_well_type text not null,
    is_positive bool not null,
    screen_result_id int4 not null,
    well_id text not null,
    primary key (assay_well_id)
);

create index assay_well_unique_index on assay_well (well_id, screen_result_id);

create index assay_well_well_positives_only_index on assay_well (is_positive) where is_positive; 

alter table assay_well 
    add constraint fk_assay_well_to_well 
    foreign key (well_id) 
    references well;

alter table assay_well 
    add constraint fk_assay_well_to_screen_result 
    foreign key (screen_result_id) 
    references screen_result;

create sequence assay_well_id_seq;

/* note: assay_well_type *should* be same for a grouping of rows, but ICCB-L legacy data does not comply */
insert into assay_well (assay_well_id, version, well_id, assay_well_type, is_positive, screen_result_id) 
select nextval('assay_well_id_seq'), 0, well_id, min(assay_well_type), bool_or(is_positive), screen_result_id from result_value rv 
join result_value_type rvt using(result_value_type_id) join screen_result sr using(screen_result_id) 
group by well_id, screen_result_id;


COMMIT;
