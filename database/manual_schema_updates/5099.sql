BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5099,
current_timestamp,
'add copy.date_plated';

alter table copy add column date_plated date;

update copy set date_plated = (select min(date_of_activity) from activity a join plate p on(p.plated_activity_id=a.activity_id) where p.copy_id = copy.copy_id);

update library set date_screenable = (select min(date_plated) from copy c where c.library_id = library.library_id);

COMMIT;
