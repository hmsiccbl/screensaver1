BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3905,
current_timestamp,
'replace result_value_type.{is_numeric,is_positive_indicator,positive_indicator_type} with data_type field; move result_value.numeric_decimal_precision to result_value_type.decimal_places';


alter table result_value_type add data_type text /*not null*/;
alter table result_value_type add decimal_places int4;

update result_value_type set data_type = 'Boolean Positive Indicator' where is_positive_indicator and positive_indicator_type = 'Boolean';
update result_value_type set data_type = 'Partition Positive Indicator' where is_positive_indicator and positive_indicator_type = 'Partition';
update result_value_type set data_type = 'Numeric' where not is_positive_indicator and is_numeric;
update result_value_type set data_type = 'Text' where not is_positive_indicator and not is_numeric;

alter table result_value_type drop column is_numeric;
alter table result_value_type drop column is_positive_indicator;
alter table result_value_type drop column positive_indicator_type;


/* note that this is destructive operation if the result_value records,
for a given result_value_type, happened to have varying decimal
precisions; all result_value records for the same result_value_type
will now use the maximum precision found within that result_value_type */
update result_value_type set decimal_places = (select max(numeric_decimal_precision) from result_value where result_value_type_id = result_value_type.result_value_type_id);
update result_value_type set decimal_places = 3 where decimal_places is null;
alter table result_value drop column numeric_decimal_precision;

alter table result_value_type alter data_type set not null;

COMMIT;
