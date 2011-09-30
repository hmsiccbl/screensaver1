BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6490,
current_timestamp,
'grant serviceActivityAdmin role to admins with existing usersAdmin role';

insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select screensaver_user_id, 'serviceActivityAdmin' from screensaver_user_role where screensaver_user_role = 'usersAdmin';

commit;