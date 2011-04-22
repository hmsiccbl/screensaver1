BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4624,
current_timestamp,
'add copy.comments';

alter table copy add column comments text;
  
COMMIT;
