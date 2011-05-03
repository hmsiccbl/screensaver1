BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3800,
current_timestamp,
'add library_screening.screened_experimental_well_count';

alter table library_screening rename column is_special to is_for_screener_provided_plates;

alter table library_screening add screened_experimental_well_count int4 not null default 0;
update library_screening set screened_experimental_well_count = (select count(w.well_id) from well w, plates_used pu where pu.library_screening_id = library_screening.activity_id and w.plate_number between pu.start_plate and pu.end_plate and w.library_well_type = 'experimental');

COMMIT;
