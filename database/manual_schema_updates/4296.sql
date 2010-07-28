BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4296,
current_timestamp,
'recluster and reindex annotation_value table';

create table annotation_value_clustered as
    select * from annotation_value order by annotation_type_id, reagent_id;
alter table annotation_value rename to annotation_value_old;
alter table annotation_value_clustered rename to annotation_value;
drop table annotation_value_old;

/* note: we are not recreating the primary key index, since it is never used in practice */
create unique index annotation_value_annotation_type_id_key on annotation_value (annotation_type_id, reagent_id);
create index annot_value_annot_type_and_value_index on annotation_value (annotation_type_id, value);
create index annot_value_annot_type_and_numeric_value_index on annotation_value (annotation_type_id, numeric_value);

alter table annotation_value 
    add constraint fk_annotation_value_to_reagent 
    foreign key (reagent_id) 
    references reagent;

alter table annotation_value 
    add constraint fk_annotation_value_to_annotation_type 
    foreign key (annotation_type_id) 
    references annotation_type;


/*cluster annotation_value using annotation_value_annotation_type_id_key;*/

analyze annotation_value;

COMMIT;
