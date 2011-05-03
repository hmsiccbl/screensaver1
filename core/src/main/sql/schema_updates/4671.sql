BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4671,
current_timestamp,
'add libraryCopiesAdmin roles';

/* TODO: this adds the libraryCopiesAdmin role
to every administrator that currently has the librariesAdmin role;
alternately, you may want to add this new role to specific
administrators only. */
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select screensaver_user_id, 'libraryCopiesAdmin' from screensaver_user_role where screensaver_user_role = 'librariesAdmin';

COMMIT;
