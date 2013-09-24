/* ICCB-L specific!  Modify as necessary for your facility's data */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20130113,
current_timestamp,
'Issue #113 Create a synonym table for vendor IDs: because the Biomol library vendor name has changed';

alter table reagent add column vendor_name_synonym text;

update reagent set vendor_name_synonym = 'BIOMOL' where vendor_name = 'Enzo Life Sciences';
update reagent set vendor_name_synonym = 'Enzo Life Sciences' where vendor_name = 'BIOMOL';

COMMIT;