BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3061,
current_timestamp,
'added userChecklistItemsAdmin role';

INSERT INTO screensaver_user_role SELECT screensaver_user_id, 'userChecklistItemsAdmin' FROM screensaver_user_role WHERE screensaver_user_role = 'usersAdmin';

COMMIT;
