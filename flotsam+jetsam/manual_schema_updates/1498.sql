BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1498;
 
ALTER TABLE screen ADD COLUMN publishable_protocol_date_entered TIMESTAMP;
ALTER TABLE screen ADD COLUMN publishable_protocol_entered_by TEXT;

COMMIT;
