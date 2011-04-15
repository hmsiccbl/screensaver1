BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5541,
current_timestamp,
'add copy.plates_available';

alter table copy add column plates_available int4;
update copy set plates_available = (select count(*) from plate p where status = 'Available' and p.copy_id = copy.copy_id);

COMMIT;
