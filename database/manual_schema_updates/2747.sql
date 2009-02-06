BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2747,
current_timestamp,
'added column channel';

alter table result_value_type add column channel integer;


COMMIT;
