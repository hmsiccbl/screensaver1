BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1288;

ALTER TABLE screensaver_user DROP CONSTRAINT screensaver_user_email_key;

COMMIT;