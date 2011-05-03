BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2126,
current_timestamp,
'added annotation_value indexes';


/*CREATE INDEX result_value_numeric_value_index ON result_value (numeric_value);*/

/*CREATE INDEX result_value_value_index ON result_value (value);*/

CREATE INDEX annotation_value_value_index ON annotation_value (value);

CREATE INDEX annotation_value_numeric_value_index ON annotation_value (numeric_value);

COMMIT;