BEGIN;


INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20140124,
current_timestamp,
'issue 113 - add antisense attribute to silencing_reagent table';

alter table silencing_reagent add column anti_sense_sequence text;

COMMIT;

