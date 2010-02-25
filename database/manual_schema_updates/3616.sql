BEGIN;

INSERT INTO schema_history(screensaver_revision, date_updated, comment)
SELECT
3616,
current_timestamp,
'rename data access roles';

update screensaver_user_role set screensaver_user_role = 'smDsl3SharedScreens' where screensaver_user_role = 'smallMoleculeScreener';

update screensaver_user_role set screensaver_user_role = 'rnaiScreens' where screensaver_user_role = 'rnaiScreener';


COMMIT;
