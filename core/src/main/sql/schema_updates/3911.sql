BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3911,
current_timestamp,
'rename assay_well_type fields to assay_well_control_type and make nullable';

alter table assay_well rename assay_well_type to assay_well_control_type;
alter table assay_well alter assay_well_control_type drop not null;
alter table result_value rename assay_well_type to assay_well_control_type;
alter table result_value alter assay_well_control_type drop not null;

COMMIT;
