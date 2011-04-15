BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2116,
current_timestamp,
'added Well.gene';

ALTER TABLE well ADD gene_id int4;

UPDATE well 
SET gene_id = sr.gene_id 
FROM silencing_reagent sr join well_silencing_reagent_link l using(silencing_reagent_id) 
WHERE l.well_id = well.well_id AND sr.silencing_reagent_id = 
   (SELECT min(silencing_reagent_id) 
    FROM well_silencing_reagent_link l2 
    WHERE l2.well_id = well.well_id);

ALTER TABLE well 
    ADD CONSTRAINT fk_well_to_gene 
    FOREIGN KEY (gene_id) 
    REFERENCES gene;

COMMIT;