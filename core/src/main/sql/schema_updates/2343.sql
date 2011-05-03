BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2343,
current_timestamp,
'added Volume type; removed Screening.estimatedFinalScreenConcentrationInMoles';

ALTER TABLE cherry_pick_request ADD COLUMN transfer_volume_per_well_approved NUMERIC(10,9);
UPDATE cherry_pick_request SET transfer_volume_per_well_approved = microliter_transfer_volume_per_well_approved / 1000000.0;
ALTER TABLE cherry_pick_request DROP COLUMN microliter_transfer_volume_per_well_approved;

ALTER TABLE cherry_pick_request ADD COLUMN transfer_volume_per_well_requested NUMERIC(10,9);
UPDATE cherry_pick_request SET transfer_volume_per_well_requested = microliter_transfer_volume_per_well_requested / 1000000.0;
ALTER TABLE cherry_pick_request DROP COLUMN microliter_transfer_volume_per_well_requested;

ALTER TABLE copy_info ADD COLUMN well_volume NUMERIC(10,9);
UPDATE copy_info SET well_volume = microliter_well_volume / 1000000.0;
ALTER TABLE copy_info DROP COLUMN microliter_well_volume;

ALTER TABLE lab_activity ADD COLUMN volume_transferred_per_well NUMERIC(10,9);
UPDATE lab_activity SET volume_transferred_per_well = microliter_volume_transfered_per_well / 1000000.0;
ALTER TABLE lab_activity DROP COLUMN microliter_volume_transfered_per_well;

ALTER TABLE well_volume_adjustment ADD COLUMN volume NUMERIC(10,9);
UPDATE well_volume_adjustment SET volume = microliter_volume / 1000000.0;
ALTER TABLE well_volume_adjustment DROP COLUMN microliter_volume;

ALTER TABLE screening DROP COLUMN estimated_final_screen_concentration_in_moles;

COMMIT;
