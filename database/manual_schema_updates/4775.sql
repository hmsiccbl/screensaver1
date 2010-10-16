BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4775,
current_timestamp,
'add screen.facility_id';

alter table screen add column facility_id text;
update screen set facility_id = cast(screen_number as text);
alter table screen alter column facility_id set not null;
alter table screen add constraint screen_facility_id_key unique (facility_id);

update screen set facility_id = 'S' || facility_id where screen_number >= 100000;

alter table screen drop column screen_number;

COMMIT;
