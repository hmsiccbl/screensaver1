BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2220,
current_timestamp,
'revert result_value primary key to int with sequence';

CREATE TABLE result_value_int_pk (
    result_value_id INTEGER NOT NULL,
    assay_well_type TEXT NOT NULL,
    is_exclude BOOL NOT NULL,
    numeric_decimal_precision INT4,
    numeric_value FLOAT8,
    is_positive BOOL NOT NULL,
    value TEXT,
    result_value_type_id INT4 NOT NULL,
    well_id TEXT NOT NULL,
    PRIMARY KEY (result_value_id)
);

CREATE SEQUENCE result_value_id_seq;

INSERT INTO result_value_int_pk (
  result_value_id,
  well_id,
  result_value_type_id,
  assay_well_type,
  is_exclude,
  value,
  numeric_value,
  numeric_decimal_precision,
  is_positive
)
SELECT
  nextval('result_value_id_seq'),
  well_id,
  result_value_type_id,
  assay_well_type,
  cast(is_exclude as BOOLEAN),
  value,
  numeric_value,
  numeric_decimal_precision,
  CAST(is_positive as BOOLEAN)
FROM result_value ORDER BY result_value_id;

DROP TABLE result_value CASCADE;
ALTER TABLE result_value_int_pk RENAME TO result_value;

/*CREATE INDEX result_value_numeric_value_index ON result_value (numeric_value);
*/
CREATE INDEX result_value_well_index ON result_value (well_id);
CREATE INDEX result_value_result_value_type_index ON result_value (result_value_type_id);
/*CREATE INDEX result_value_is_positive_index ON result_value (is_positive);*/
/*CREATE INDEX result_value_value_index ON result_value (value);*/
ALTER TABLE result_value 
    ADD CONSTRAINT fk_result_value_to_result_value_type 
    FOREIGN KEY (result_value_type_id) 
    REFERENCES result_value_type;
ALTER TABLE result_value 
    ADD CONSTRAINT fk_result_value_to_well 
    FOREIGN KEY (well_id) 
    REFERENCES well;

COMMIT;