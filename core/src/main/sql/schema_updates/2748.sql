BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2748,
current_timestamp,
'added columns plate_rows and plate_columns to library table';

ALTER TABLE library ADD COLUMN plate_rows INTEGER;
ALTER TABLE library ADD COLUMN plate_columns INTEGER;


UPDATE library 
set plate_rows = 16;

UPDATE library 
set plate_columns = 24;


ALTER TABLE library ALTER COLUMN plate_rows SET NOT NULL;
ALTER TABLE library ALTER COLUMN plate_columns SET NOT NULL;


COMMIT;
