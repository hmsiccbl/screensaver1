BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4395,
current_timestamp,
'add partitioned positives counts';

alter table data_column add column strong_positives_count int;
alter table data_column add column medium_positives_count int;
alter table data_column add column weak_positives_count int;

update data_column set strong_positives_count = (select count(*) from result_value rv where value = '3' and rv.data_column_id = data_column.data_column_id) where data_type = 'Partition Positive Indicator';
update data_column set medium_positives_count = (select count(*) from result_value rv where value = '2' and rv.data_column_id = data_column.data_column_id) where data_type = 'Partition Positive Indicator';
update data_column set weak_positives_count = (select count(*) from result_value rv where value = '1' and rv.data_column_id = data_column.data_column_id) where data_type = 'Partition Positive Indicator';

COMMIT;
