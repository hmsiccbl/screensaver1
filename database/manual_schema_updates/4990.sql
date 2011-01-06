BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4990,
current_timestamp,
'add plate.status';

alter table plate add column status text;
update plate set status = 'Retired' where date_retired is not null;
update plate set status = 'Available' where status is null;
alter table plate alter column status set not null;

COMMIT;
