BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3003,
current_timestamp,
'replaced library.{columns,rows} with plate_size';

ALTER TABLE library DROP COLUMN plate_rows;
ALTER TABLE library DROP COLUMN plate_columns;
ALTER TABLE library ADD COLUMN plate_size text;
UPDATE library set plate_size = '384';
ALTER TABLE library ALTER plate_size SET NOT NULL;

COMMIT;