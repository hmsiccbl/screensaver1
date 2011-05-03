BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4335,
current_timestamp,
'rename library vendor to provider';

alter table library rename column vendor to provider;

COMMIT;
