BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1949,
current_timestamp,
'added unique constraint for screensaver_user.login_id';

alter table screensaver_user add constraint screensaver_user_login_id_key unique (login_id);

COMMIT;