BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4053,
current_timestamp,
'add screen result data loading activities';

/* create a new screen_result_data_loading activity for each screen result */

create temp table screen_result_data_loading (screen_result_id int, screen_result_data_loading_id int);
insert into screen_result_data_loading (screen_result_id, screen_result_data_loading_id)
select screen_result_id, nextval('activity_id_seq') from screen_result;

/* TODO: must update administrator user IDs */
insert into activity (activity_id, date_created, date_of_activity, performed_by_id, created_by_id, comments, version)
select srdl.screen_result_data_loading_id, sr.date_last_imported, sr.date_last_imported, 761, 755, 'added automatically for data migration', 0
from screen_result_data_loading srdl join screen_result sr using(screen_result_id);

insert into administrative_activity (activity_id, administrative_activity_type)
select srdl.screen_result_data_loading_id, 'Screen Result Data Loading'
from screen_result_data_loading srdl join screen_result sr using(screen_result_id);

/* create missing assay plates, which can occur in legacy data if library screenings fail to define all of the assay plates that the comprise the screen result */

/* TODO: create sufficient rows to match: select max(replicate_ordinal) from data_column; */
create temp table replicate_ordinal (replicate_ordinal int);
insert into replicate_ordinal (replicate_ordinal) values (0);
insert into replicate_ordinal (replicate_ordinal) values (1);
insert into replicate_ordinal (replicate_ordinal) values (2);
insert into replicate_ordinal (replicate_ordinal) values (3);

/* this index apparently need by postgres 8.3 for the following insert/select statement, while 8.4 can create an efficient query plan without it  */
create index assay_plate_screen_and_plate_number on assay_plate (screen_id, plate_number); 

insert into assay_plate (assay_plate_id, screen_id, plate_number, replicate_ordinal, version)
select nextval('assay_plate_id_seq'), sr.screen_id, x.plate_number, r.replicate_ordinal, 0
from
screen_result sr join replicate_ordinal r on(r.replicate_ordinal < sr.replicate_count) join
(select distinct sr.screen_result_id, w.plate_number from screen_result sr join assay_well aw using(screen_result_id) join well w using(well_id) where not exists (select * from assay_plate ap where ap.screen_id = sr.screen_id and ap.plate_number = w.plate_number)) as x using(screen_result_id);

drop index assay_plate_screen_and_plate_number;


/* associate the new screen_result_data_loading activities with their assay plates */

update assay_plate set screen_result_data_loading_id = srdl.screen_result_data_loading_id 
from screen_result_data_loading srdl join screen_result sr using(screen_result_id) 
where assay_plate.screen_id = sr.screen_id
and exists (select * from assay_well aw join well w using(well_id) where aw.screen_result_id = sr.screen_result_id and w.plate_number = assay_plate.plate_number)
and assay_plate.screen_result_data_loading_id is null;

update screen set library_plates_data_loaded_count = (select count(distinct plate_number) from assay_plate ap where ap.screen_id = screen.screen_id and screen_result_data_loading_id is not null);

update screen set library_plates_data_analyzed_count = library_plates_data_loaded_count;

/* TODO: verify post-condition: should be empty result, indicating that the size of the old plate number set equals the library_plates_data_loaded_count:
select screen_number, count(*) as plate_numbers, library_plates_data_loaded_count from screen s join screen_result sr using(screen_id) join screen_result_plate_number srpn using(screen_result_id) group by screen_number, library_plates_data_loaded_count having count(*) <> library_plates_data_loaded_count;
*/

drop table screen_result_plate_number;

alter table screen_result drop column date_last_imported;


COMMIT;
