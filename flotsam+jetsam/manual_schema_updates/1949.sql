BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1948,
current_timestamp,
'added unique constraints for screensaver_user';

alter table screensaver_user add constraint screensaver_user_login_id_key unique (login_id);

COMMIT;