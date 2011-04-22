BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4051,
current_timestamp,
'add screening status counts to screen';

alter table screen add column assay_plates_screened_count int4 not null default 0;
alter table screen add column library_plates_screened_count int4 not null default 0;
alter table screen add column library_plates_data_loaded_count int4 not null default 0;
alter table screen add column library_plates_data_analyzed_count int4 not null default 0;

update screen set assay_plates_screened_count = (select count(*) from assay_plate ap where ap.screen_id = screen.screen_id and library_screening_id is not null);
update screen set library_plates_screened_count = (select count(distinct plate_number) from assay_plate ap where ap.screen_id = screen.screen_id and library_screening_id is not null);

COMMIT;
