BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6410,
current_timestamp,
'add RNAi data sharing levels 1 and 3';

update screensaver_user_role set screensaver_user_role = 'rnaiDsl3SharedScreens' where screensaver_user_role = 'rnaiScreens';

commit;
