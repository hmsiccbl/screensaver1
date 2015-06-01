BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20150601,
current_timestamp,
'add barcode field to well';

alter table well add column barcode text;
alter table well add constraint barcode_unique UNIQUE(barcode);
COMMIT;