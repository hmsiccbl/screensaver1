BEGIN;


INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7304,
current_timestamp,
'for #3571 Change AssayWellReadoutType: "Photometry" to "Absorbance"';

/* TODO: ICCBL-specific update */
update data_column set assay_readout_type = 'Absorbance' where assay_readout_type = 'Photometry';
COMMIT;

