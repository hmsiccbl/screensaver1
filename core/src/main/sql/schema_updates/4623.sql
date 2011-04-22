BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4623,
current_timestamp,
'add plate.barcode';

alter table plate add column barcode text;
  
COMMIT;
