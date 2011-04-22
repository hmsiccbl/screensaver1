BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1594, 
current_timestamp, 
'well_volume_adjustment.microliter_volume not null';
 
ALTER TABLE well_volume_adjustment ALTER COLUMN microliter_volume set not null;

COMMIT;