BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1807,
current_timestamp,
'annotation tweaks';

DROP TABLE annotation CASCADE;
DROP TABLE annotation_value CASCADE;
DROP TABLE annotation_value_well_link;
DROP SEQUENCE annotation_id_seq;

CREATE TABLE annotation_type (
    annotation_type_id INT4 NOT NULL,
    version INT4 NOT NULL,
    study_id INT4 NOT NULL,
    name VARCHAR(255),
    description TEXT,
    ordinal INT4,
    numeric BOOL NOT NULL,
    PRIMARY KEY (annotation_type_id)
);

CREATE TABLE annotation_value (
    annotation_value_id INT4 NOT NULL,
    version INT4 NOT NULL,
    annotation_type_id INT4 NOT NULL,
    vendor_identifier TEXT NOT NULL,
    value TEXT,
    numeric_value NUMERIC(19, 2),
    PRIMARY KEY (annotation_value_id)
);

ALTER TABLE annotation_type 
    ADD CONSTRAINT fk_annotation_type_to_screen 
    FOREIGN KEY (study_id) 
    REFERENCES screen;

ALTER TABLE annotation_value 
    ADD CONSTRAINT fk_annotation_value_to_annotation_type 
    FOREIGN KEY (annotation_type_id) 
    REFERENCES annotation_type;

CREATE SEQUENCE annotation_type_id_seq;

COMMIT;