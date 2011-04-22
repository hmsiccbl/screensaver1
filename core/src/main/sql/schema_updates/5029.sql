BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5029,
current_timestamp,
'add concentration to plate';

alter table plate add column concentration numeric(12, 9);

COMMIT;
