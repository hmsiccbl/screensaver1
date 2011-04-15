BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3760,
current_timestamp,
'add cherry_pick_assay_plate relationship to cherry_pick_screening';

ALTER TABLE cherry_pick_assay_plate ADD COLUMN cherry_pick_screening_id int4;

ALTER TABLE cherry_pick_assay_plate 
    ADD CONSTRAINT fk_cherry_pick_assay_plate_to_cherry_pick_screening 
    FOREIGN KEY (cherry_pick_screening_id) 
    REFERENCES cherry_pick_screening;

COMMIT;
