BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5269,
current_timestamp,
'rename concentration field to molar_concentration';

alter table well rename concentration to molar_concentration;

COMMIT;
