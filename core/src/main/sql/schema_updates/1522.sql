BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1522, 
current_timestamp, 
'xfer copy_info_microliter_well_volumes to well_volume_adjustment; drop copy_info_microliter_well_volumes; negate well_volume_adjustment.volumes related to lab_cherry_pick rows';
 
UPDATE well_volume_adjustment SET microliter_volume = microliter_volume * -1 WHERE lab_cherry_pick_id is not null;

INSERT INTO well_volume_adjustment (well_volume_adjustment_id, version, copy_id, well_id, microliter_volume) SELECT nextval('well_volume_adjustment_id_seq'), 0, ci.copy_id, cimwv.well_key, (cimwv.microliter_volume - ci.microliter_well_volume) FROM copy_info_microliter_well_volumes cimwv JOIN copy_info ci using (copy_info_id);

DROP TABLE copy_info_microliter_well_volumes;

COMMIT;