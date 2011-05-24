BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5313,
current_timestamp,
'add facility_batch_id, salt_form_id, vendor_batch_id to small_molecule_reagent';

alter table small_molecule_reagent add facility_batch_id int4;
alter table small_molecule_reagent add salt_form_id int4;
alter table small_molecule_reagent add vendor_batch_id text;

COMMIT;
