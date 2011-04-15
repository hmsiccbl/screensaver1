BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2151,
current_timestamp,
'added result_value.rvt_and_{numeric,}value indexes';

/*DROP INDEX result_value_is_positive_index;*/
/*DROP INDEX result_value_value_index;*/
/*DROP INDEX result_value_numeric_value_index;*/

CREATE INDEX result_value_rvt_and_value_index ON result_value (result_value_type_id, value);
CREATE INDEX result_value_rvt_and_numeric_value_index ON result_value (result_value_type_id, numeric_value);

COMMIT;