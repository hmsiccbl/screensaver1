BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3906,
current_timestamp,
'renamed result_value_type to data_column';

alter table result_value_type drop constraint fk_result_value_type_to_screen_result;
alter table result_value_type_derived_from_link drop constraint fk_derived_result_value_type;
alter table result_value_type_derived_from_link drop constraint fk_derived_from_result_value_type;
alter table result_value drop constraint fk_result_value_to_result_value_type;

alter table result_value_type rename to data_column;
alter table data_column rename result_value_type_id to data_column_id;

alter table result_value_type_derived_from_link rename to data_column_derived_from_link;
alter table data_column_derived_from_link rename derived_from_result_value_type_id to derived_from_data_column_id;
alter table data_column_derived_from_link rename derived_result_value_type_id to derived_data_column_id;

alter table result_value rename result_value_type_id to data_column_id;

alter table data_column 
    add constraint fk_data_column_to_screen_result 
    foreign key (screen_result_id) 
    references screen_result;

alter table data_column_derived_from_link 
    add constraint fk_derived_from_data_column 
    foreign key (derived_from_data_column_id) 
    references data_column;

alter table data_column_derived_from_link 
    add constraint fk_derived_data_column 
    foreign key (derived_data_column_id) 
    references data_column;

alter table result_value 
    add constraint fk_result_value_to_data_column 
    foreign key (data_column_id) 
    references data_column;

alter index result_value_result_value_type_index rename to result_value_data_column_index;
alter index result_value_rvt_and_positive_index rename to result_value_data_column_and_positive_index;
alter index result_value_rvt_and_value_index rename to result_value_data_column_and_value_index;
alter index result_value_rvt_and_numeric_value_index rename to result_value_data_column_and_numeric_value_index;

alter sequence result_value_type_id_seq rename to data_column_id_seq;


COMMIT;
