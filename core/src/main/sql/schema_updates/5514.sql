BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5514,
current_timestamp,
'added stock plate mapping fields to plate';

alter table plate add stock_plate_number int4;
alter table plate add quadrant int4;

COMMIT;
