BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4117,
current_timestamp,
'added replicate counts to screen';

alter table screen add column min_screened_replicate_count int4;
alter table screen add column max_screened_replicate_count int4;
alter table screen add column min_data_loaded_replicate_count int4;
alter table screen add column max_data_loaded_replicate_count int4;

update screen set min_screened_replicate_count = (select min(max_plate_replicate_ordinal)+1 from (select max(replicate_ordinal) as max_plate_replicate_ordinal from assay_plate ap where ap.screen_id = screen.screen_id and library_screening_id is not null group by ap.plate_number) as x);
update screen set max_screened_replicate_count = (select max(replicate_ordinal)+1 from assay_plate ap where ap.screen_id = screen.screen_id and library_screening_id is not null);
update screen set min_data_loaded_replicate_count = (select min(max_plate_replicate_ordinal)+1 from (select max(replicate_ordinal) as max_plate_replicate_ordinal from assay_plate ap where ap.screen_id = screen.screen_id and screen_result_data_loading_id is not null) as x);
update screen set max_data_loaded_replicate_count = (select max(replicate_ordinal)+1 from assay_plate ap where ap.screen_id = screen.screen_id and screen_result_data_loading_id is not null);

COMMIT;
