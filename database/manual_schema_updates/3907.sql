BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3907,
current_timestamp,
'result_value.value set to null for numeric data columns, since it is never used';

update result_value set value = null from data_column dc where result_value.data_column_id = dc.data_column_id and dc.data_type = 'Numeric';

COMMIT;
