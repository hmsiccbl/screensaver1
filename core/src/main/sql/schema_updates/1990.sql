BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1990,
current_timestamp,
'added Reagent entity';

CREATE TABLE reagent (
    reagent_id TEXT NOT NULL,
    version INT4 NOT NULL,
    PRIMARY KEY (reagent_id)
);

CREATE TABLE study_reagent_link (
    study_id INT4 NOT NULL,
    reagent_id TEXT NOT NULL,
    PRIMARY KEY (study_id, reagent_id)
);

ALTER TABLE annotation_value ADD COLUMN reagent_id text;
UPDATE annotation_value SET reagent_id = vendor_name||':'||vendor_identifier;
ALTER TABLE annotation_value ALTER COLUMN reagent_id SET NOT NULL;
ALTER TABLE annotation_value DROP COLUMN vendor_name;
ALTER TABLE annotation_value DROP COLUMN vendor_identifier;

ALTER TABLE well ADD COLUMN reagent_id text;
UPDATE well SET reagent_id = coalesce(l.vendor, '')||':'||vendor_identifier FROM library l WHERE l.library_id = well.library_id AND well.vendor_identifier IS NOT NULL;
ALTER TABLE well DROP COLUMN vendor_identifier;

INSERT INTO reagent (reagent_id, version) SELECT distinct reagent_id, 1 FROM well WHERE reagent_id IS NOT NULL;

INSERT INTO study_reagent_link (study_id, reagent_id) SELECT distinct s.screen_id, v.reagent_id FROM screen s JOIN annotation_type t ON(t.study_id=s.screen_id) JOIN annotation_value v USING(annotation_type_id);  

ALTER TABLE annotation_value 
    ADD CONSTRAINT fk_annotation_value_to_reagent 
    FOREIGN KEY (reagent_id) 
    REFERENCES reagent;

ALTER TABLE study_reagent_link 
    ADD CONSTRAINT FK352B361D161EA629 
    FOREIGN KEY (reagent_id) 
    REFERENCES reagent;

ALTER TABLE study_reagent_link 
    ADD CONSTRAINT fk_reagent_link_to_study 
    FOREIGN KEY (study_id) 
    REFERENCES screen;

ALTER TABLE well 
    ADD CONSTRAINT fk_well_to_reagent 
    FOREIGN KEY (reagent_id) 
    REFERENCES reagent;

CREATE INDEX well_reagent_id_index ON well (reagent_id);

CREATE INDEX annotation_value_reagent_id_index ON annotation_value (reagent_id);

COMMIT;