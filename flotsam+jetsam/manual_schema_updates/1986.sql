BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1986,
current_timestamp,
'added unique constraint for screen.screen_number; dropped unique constraint screensaver_user.screensaver_user_first_last_date_key';

ALTER TABLE screen ADD CONSTRAINT screen_screen_number_key UNIQUE (screen_number);

ALTER TABLE screensaver_user DROP CONSTRAINT screensaver_user_first_last_date_key;

COMMIT;