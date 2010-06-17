/*cluster result_value using result_value_data_column_index;*/
drop table if exists result_value_clustered;
create table result_value_clustered as
    select * from result_value order by data_column_id, well_id;

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4197,
current_timestamp,
'recluster result_value table on data_column_index';

alter table result_value rename to result_value_old;
alter table result_value_clustered rename to result_value;

drop index if exists result_value_data_column_index;
drop index if exists result_value_well_index;
drop index if exists result_value_data_column_and_numeric_value_index;
drop index if exists result_value_data_column_and_value_index;

create index result_value_data_column_index on result_value (data_column_id);
create index result_value_well_index on result_value (well_id);
create index result_value_data_column_and_numeric_value_index on result_value (data_column_id, numeric_value);
create index result_value_data_column_and_value_index on result_value (data_column_id, value);
/*create index result_value_data_column_and_positive_index on result_value (data_column_id, is_positive);*/
/*create index result_value_is_positive_index on result_value (is_positive);*/

alter table result_value 
    add constraint fk_result_value_to_data_column 
    foreign key (data_column_id) 
    references data_column;

alter table result_value 
    add constraint fk_result_value_to_well 
    foreign key (well_id) 
    references well;

drop table result_value_old;

analyze;

COMMIT;
