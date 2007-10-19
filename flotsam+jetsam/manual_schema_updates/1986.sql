BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1986,
current_timestamp,
'added unique constraint for screen.screen_number';

ALTER TABLE screen ADD CONSTRAINT screen_screen_number_key UNIQUE (screen_number);

/* the following must have been removed manually on dev and prod dbs, as it isn't there */
/*ALTER TABLE screensaver_user DROP CONSTRAINT screensaver_user_first_last_date_key;*/

COMMIT;