BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3809,
current_timestamp,
'add screen.{unique_,}screened_experimental_well_count';

alter table screen add screened_experimental_well_count int4 not null default 0;
update screen set screened_experimental_well_count = (select count(w.well_id) from well w, lab_activity la join library_screening ls using(activity_id) join plates_used pu on(pu.library_screening_id = ls.activity_id) where w.plate_number between pu.start_plate and pu.end_plate and w.library_well_type = 'experimental' and la.screen_id = screen.screen_id);

alter table screen add unique_screened_experimental_well_count int4 not null default 0;
update screen set unique_screened_experimental_well_count = (select count(distinct w.well_id) from well w, lab_activity la join library_screening ls using(activity_id) join plates_used pu on(pu.library_screening_id = ls.activity_id) where w.plate_number between pu.start_plate and pu.end_plate and w.library_well_type = 'experimental' and la.screen_id = screen.screen_id);

COMMIT;
