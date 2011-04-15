BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4596,
current_timestamp,
'remove plate null constraints';

alter table plate alter location drop not null;
alter table plate alter plate_type drop not null;
alter table plate alter well_volume drop not null;

COMMIT;
