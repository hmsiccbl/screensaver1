BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1358;

ALTER TABLE cherry_pick_liquid_transfer ADD COLUMN status text;
UPDATE cherry_pick_liquid_transfer SET status = 'Successful' where successful = 't';
UPDATE cherry_pick_liquid_transfer SET status = 'Failed' where successful = 'f';
ALTER TABLE cherry_pick_liquid_transfer ALTER COLUMN status SET NOT NULL;
ALTER TABLE cherry_pick_liquid_transfer DROP COLUMN successful;
/* okay to drop 'canceled' column w/o creating respective row in cherry_pick_liquid_transfer, since we know no production data exists yet for cherry pick requests! */
ALTER TABLE cherry_pick_assay_plate DROP COLUMN canceled;

COMMIT;
