BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4130,
current_timestamp,
'added {screen,library_screening}.libraries_screened_count';

alter table screen add column libraries_screened_count int4;

update screen set libraries_screened_count = (select count(distinct library_id) from assay_plate ap join plate p using(plate_id) join copy c using(copy_id) where ap.screen_id = screen.screen_id);

alter table library_screening add column libraries_screened_count int4;
alter table library_screening add column library_plates_screened_count int4;

update library_screening set libraries_screened_count = (select count(distinct library_id) from assay_plate ap join plate p using(plate_id) join copy c using(copy_id) where ap.library_screening_id = library_screening.activity_id);
update library_screening set library_plates_screened_count = (select count(distinct p.plate_number) from assay_plate ap join plate p using(plate_id) where ap.library_screening_id = library_screening.activity_id);


COMMIT;
