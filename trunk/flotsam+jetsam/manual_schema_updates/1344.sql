BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1344;

ALTER TABLE cherry_pick_assay_plate ADD COLUMN canceled bool;
/* safe to do this, but only because we know the system is not in use yet! */
UPDATE cherry_pick_assay_plate SET canceled = false;
ALTER TABLE cherry_pick_assay_plate ALTER COLUMN canceled SET NOT NULL;

COMMIT;
