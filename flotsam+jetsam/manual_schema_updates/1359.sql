BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1359;

ALTER TABLE cherry_pick_assay_plate ADD COLUMN legacy_plate_name text;
ALTER TABLE cherry_pick_assay_plate ADD COLUMN is_legacy bool;
UPDATE cherry_pick_assay_plate set is_legacy = 't', legacy_plate_name = substr(comments, 44, length(comments) - 45) where strpos(comments, 'assay plate is') > 0;
UPDATE cherry_pick_assay_plate set is_legacy = 'f' where is_legacy is null;
ALTER TABLE cherry_pick_assay_plate ALTER COLUMN is_legacy bool SET NOT NULL;
ALTER TABLE cherry_pick_assay_plate DROP COLUMN comments;

COMMIT;
