BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6479,
current_timestamp,
'move lab_activity.volume_transferred_per_well to screening.volume_transferred_per_well_to_assay_plates; add lab_activity.volume_transferred_per_well_from_library_plates';

alter table screening add column volume_transferred_per_well_to_assay_plates numeric(10, 9);
update screening set volume_transferred_per_well_to_assay_plates = la.volume_transferred_per_well from lab_activity la where la.activity_id = screening.activity_id;

alter table lab_activity rename column volume_transferred_per_well to volume_transferred_per_well_from_library_plates;
update lab_activity set volume_transferred_per_well_from_library_plates = s.volume_transferred_per_well_to_assay_plates * s.number_of_replicates from screening s where s.activity_id = lab_activity.activity_id;

commit;
