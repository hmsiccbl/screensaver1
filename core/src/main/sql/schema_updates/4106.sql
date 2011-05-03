BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4106,
current_timestamp,
'removed screen_result_well_link table';

drop table screen_result_well_link;

COMMIT;
