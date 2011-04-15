BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4935,
current_timestamp,
'rename well type buffer to RNAi buffer';

update well set library_well_type = 'RNAi buffer' where library_well_type = 'buffer';

COMMIT;
