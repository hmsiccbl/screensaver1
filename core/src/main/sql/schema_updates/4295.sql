drop table if exists result_value_clustered;
create table result_value_clustered as
    select * from result_value order by data_column_id, well_id;

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4295,
current_timestamp,
'recluster and reindex result_value table';

alter table result_value rename to result_value_old;
alter table result_value_clustered rename to result_value;

drop index if exists result_value_data_column_and_numeric_value_index;
drop index if exists result_value_data_column_and_value_index;

/* note: we are not recreating the primary key index, since it is never used in practice */
create unique index result_value_data_column_and_well_index on result_value (data_column_id, well_id);
create index result_value_data_column_and_numeric_value_index on result_value (data_column_id, numeric_value);
create index result_value_data_column_and_value_index on result_value (data_column_id, value);

alter table result_value 
    add constraint fk_result_value_to_data_column 
    foreign key (data_column_id) 
    references data_column;

alter table result_value 
    add constraint fk_result_value_to_well 
    foreign key (well_id) 
    references well;

drop table result_value_old;

/*cluster result_value using result_value_data_column_and_well_index;*/

analyze result_value;

COMMIT;
