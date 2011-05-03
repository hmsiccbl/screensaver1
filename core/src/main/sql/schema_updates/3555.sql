BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3555,
current_timestamp,
'added Screen.dataPrivacyExpirationDate';

alter table screen add column data_privacy_expiration_date date;

COMMIT;
