BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4576,
current_timestamp,
'add field assay_well_volume to library_screening';

alter table screening add column assay_well_volume numeric(10, 9);

COMMIT;
