BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4085,
current_timestamp,
'drop screen_result.comments';

/* TODO: verify screen_result.comments is not used:
 * 
 * select count(*) from screen_result where length(trim(comments)) > 0;
 */
alter table screen_result drop column comments;

COMMIT;
