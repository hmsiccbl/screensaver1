BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2505,
current_timestamp,
'added Well.deprecationActivity; AdministrativeActivity is now a concrete entity';

ALTER TABLE well ADD COLUMN deprecation_admin_activity INT4;

ALTER TABLE well 
    ADD CONSTRAINT fk_well_to_deprecation_admin_activity 
    FOREIGN KEY (deprecation_admin_activity) 
    REFERENCES administrative_activity;

ALTER TABLE administrative_activity ADD COLUMN administrative_activity_type TEXT;
UPDATE administrative_activity SET administrative_activity_type = 'Well Volume Correction';
ALTER TABLE administrative_activity ALTER COLUMN administrative_activity_type SET NOT NULL;

COMMIT;
