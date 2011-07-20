/*
 * NOTE: this data migration script should not be necessary, since these fields are unused, at this time, outside of LINCS
 */
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6092,
current_timestamp,
'refactor: move  facility_batch_id, vendor_batch_id from small_molecule_reagent to reagent';

alter table small_molecule_reagent drop column facility_batch_id;
alter table small_molecule_reagent drop column vendor_batch_id;

alter table reagent add facility_batch_id int4;
alter table reagent add vendor_batch_id text;

COMMIT;
