BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1749,
current_timestamp, 
'added library annotations';

CREATE TABLE annotation (
    annotation_id INT4 NOT NULL,
    version INT4 NOT NULL,
    study_id INT4 NOT NULL,
    name VARCHAR(255),
    description VARCHAR(255),
    numeric BOOL NOT NULL,
    PRIMARY KEY (annotation_id)
);

CREATE TABLE annotation_value (
    annotation_value_id INT4 NOT NULL,
    version INT4 NOT NULL,
    value TEXT,
    numeric_value NUMERIC(19, 2),
    annotation_id INT4 NOT NULL,
    PRIMARY KEY (annotation_value_id)
);

CREATE TABLE annotation_value_well_link (
    annotation_value_id INT4 NOT NULL,
    well_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (annotation_value_id, well_id)
);

ALTER TABLE annotation 
    ADD CONSTRAINT fk_annotation_to_screen 
    FOREIGN KEY (study_id) 
    REFERENCES screen;

ALTER TABLE annotation_value 
    ADD CONSTRAINT fk_annotation_value_to_annotation
    FOREIGN KEY (annotation_id) 
    REFERENCES annotation;

ALTER TABLE annotation_value_well_link 
    ADD CONSTRAINT FK180A4E6D11EE135B 
    FOREIGN KEY (annotation_value_id) 
    REFERENCES annotation_value;

ALTER TABLE annotation_value_well_link 
    ADD CONSTRAINT FK180A4E6D433A43AB 
    FOREIGN KEY (well_id) 
    REFERENCES well;

CREATE SEQUENCE annotation_id_seq;

CREATE SEQUENCE annotation_value_id_seq;

ALTER TABLE screen ADD COLUMN study_type TEXT;
UPDATE SCREEN SET study_type = 'In vitro';
ALTER TABLE screen ALTER COLUMN study_type SET NOT NULL;

COMMIT;