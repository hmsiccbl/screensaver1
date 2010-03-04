BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3873,
current_timestamp,
'remove "Numerical" positive indicator type from result_value_type';

update result_value_type set how_derived = (case when how_derived is null then '' else how_derived || ' ' end) || '[positives determined using cutoff value of ' || positive_indicator_cutoff || ' where ' || positive_indicator_direction || ' a positive]' where positive_indicator_type = 'Numerical';
update result_value_type set positive_indicator_type = 'Boolean' where positive_indicator_type = 'Numerical';

alter table result_value_type drop column positive_indicator_cutoff;
alter table result_value_type drop column positive_indicator_direction;

COMMIT;
