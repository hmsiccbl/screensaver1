BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4807,
current_timestamp,
'renamed plate.barcode to plate facility_id';

alter table plate rename column barcode to facility_id;
  
COMMIT;
