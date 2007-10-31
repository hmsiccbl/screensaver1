BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2002,
current_timestamp,
'convert ResultValue into an entity; ';


CREATE TABLE result_value (
    result_value_id INT4 NOT NULL,
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

ALTER TABLE result_value
    ADD CONSTRAINT fk_result_value_to_result_value_type
    FOREIGN KEY (result_value_type_id)
    REFERENCES result_value_type;

ALTER TABLE result_value
    ADD CONSTRAINT fk_result_value_to_well
    FOREIGN KEY (well_id)
    REFERENCES well;

ALTER TABLE study_reagent_link DROP CONSTRAINT FK352B361D161EA629;
ALTER TABLE study_reagent_link
    ADD CONSTRAINT fk_reagent_to_study
    FOREIGN KEY (reagent_id)
    REFERENCES reagent;

CREATE SEQUENCE result_value_id_seq;

INSERT INTO result_value (result_value_id, assay_well_type, is_exclude, numeric_decimal_precision, numeric_value, is_positive, value, result_value_type_id, well_id) SELECT nextval('result_value_id_seq'), assay_well_type, is_exclude, numeric_decimal_precision, numeric_value, is_positive, value, result_value_type, well_id FROM result_value_type_result_values;

CREATE INDEX result_value_well_index ON result_value (well_id);

CREATE INDEX result_value_result_value_type_index ON result_value (result_value_type_id);

CREATE INDEX result_value_is_positive_index ON result_value (is_positive);

DROP TABLE result_value_type_result_values;

COMMIT;