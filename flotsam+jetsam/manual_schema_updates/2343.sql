BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2343,
current_timestamp,
'added Volume type; removed Screening.estimatedFinalScreenConcentrationInMoles';

ALTER TABLE cherry_pick_request RENAME COLUMN microliter_transfer_volume_per_well_approved TO transfer_volume_per_well_approved;
ALTER TABLE cherry_pick_request ALTER COLUMN transfer_volume_per_well_approved TYPE NUMERIC(12,9);
UPDATE cherry_pick_request SET transfer_volume_per_well_approved = transfer_volume_per_well_approved / (1000.0*1000.0);
ALTER TABLE cherry_pick_request ALTER COLUMN transfer_volume_per_well_approved TYPE NUMERIC(10,9);

ALTER TABLE cherry_pick_request RENAME COLUMN microliter_transfer_volume_per_well_requested TO transfer_volume_per_well_requested;
ALTER TABLE cherry_pick_request ALTER COLUMN transfer_volume_per_well_requested TYPE NUMERIC(12,9);
UPDATE cherry_pick_request SET transfer_volume_per_well_requested = transfer_volume_per_well_requested / (1000.0*1000.0);
ALTER TABLE cherry_pick_request ALTER COLUMN transfer_volume_per_well_requested TYPE NUMERIC(10,9);

ALTER TABLE copy_info RENAME COLUMN microliter_well_volume TO well_volume;
ALTER TABLE copy_info ALTER COLUMN well_volume TYPE NUMERIC(12,9);
UPDATE copy_info SET well_volume = well_volume / (1000.0*1000.0);
ALTER TABLE copy_info ALTER COLUMN well_volume TYPE NUMERIC(10,9);

ALTER TABLE lab_activity RENAME COLUMN microliter_volume_transfered_per_well TO volume_transferred_per_well;
ALTER TABLE lab_activity ALTER COLUMN volume_transferred_per_well TYPE NUMERIC(12,9);
UPDATE lab_activity SET volume_transferred_per_well = volume_transferred_per_well / (1000.0*1000.0);
ALTER TABLE lab_activity ALTER COLUMN volume_transferred_per_well TYPE NUMERIC(10,9);

ALTER TABLE well_volume_adjustment RENAME COLUMN microliter_volume TO volume;
ALTER TABLE well_volume_adjustment ALTER COLUMN volume TYPE NUMERIC(12,9);
UPDATE well_volume_adjustment SET volume = volume / (1000.0*1000.0);
ALTER TABLE well_volume_adjustment ALTER COLUMN volume TYPE NUMERIC(10,9);

ALTER TABLE screening DROP COLUMN estimated_final_screen_concentration_in_moles;

COMMIT;
