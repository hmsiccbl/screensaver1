BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2002,
current_timestamp,
'convert ResultValue into an entity';

ALTER TABLE study_reagent_link DROP CONSTRAINT FK352B361D161EA629;
ALTER TABLE study_reagent_link
    ADD CONSTRAINT fk_reagent_to_study
    FOREIGN KEY (reagent_id)
    REFERENCES reagent;

CREATE SEQUENCE result_value_id_seq;

ALTER TABLE result_value_type_result_values DROP CONSTRAINT fkbfc506afeead059e;
ALTER TABLE result_value_type_result_values DROP CONSTRAINT result_value_type_result_values_well_id_fkey;
ALTER TABLE result_value_type_result_values DROP CONSTRAINT result_value_type_result_values_pkey;
DROP INDEX rvt_index;
DROP INDEX wellkey_index;
ALTER TABLE result_value_type_result_values RENAME TO result_value;
ALTER TABLE result_value ADD COLUMN result_value_id INT4;
UPDATE result_value set result_value_id = nextval('result_value_id_seq');
ALTER TABLE result_value ALTER COLUMN result_value_id SET NOT NULL;
ALTER TABLE result_value ADD PRIMARY KEY (result_value_id);
ALTER TABLE result_value RENAME COLUMN result_value_type TO result_value_type_id;
CREATE INDEX result_value_well_index ON result_value (well_id);
CREATE INDEX result_value_result_value_type_index ON result_value (result_value_type_id);
CREATE INDEX result_value_is_positive_index ON result_value (is_positive);

ALTER TABLE result_value
    ADD CONSTRAINT fk_result_value_to_result_value_type
    FOREIGN KEY (result_value_type_id)
    REFERENCES result_value_type;

ALTER TABLE result_value
    ADD CONSTRAINT fk_result_value_to_well
    FOREIGN KEY (well_id)
    REFERENCES well;

COMMIT;