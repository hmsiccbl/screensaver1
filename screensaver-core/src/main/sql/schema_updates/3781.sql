BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3781,
current_timestamp,
'added column screen_result.channel_count';

alter table screen_result  add column channel_count integer;

COMMIT;