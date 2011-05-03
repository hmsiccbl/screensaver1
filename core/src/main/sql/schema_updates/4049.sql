BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4049,
current_timestamp,
'add assay_plate table';


create table assay_plate (
    assay_plate_id int4 not null,
    replicate_ordinal int4 not null,
    version int4 not null,
    library_screening_id int4,
    screen_id int4 not null,
    plate_id int4,
    plate_number int4 not null,
    screen_result_data_loading_id int4,
    primary key (assay_plate_id),
    unique (library_screening_id, plate_number, replicate_ordinal)
);

alter table assay_plate 
    add constraint fk_assay_plate_to_screen_result_data_loading 
    foreign key (screen_result_data_loading_id) 
    references administrative_activity;

alter table assay_plate 
    add constraint fk_assay_plate_to_plate 
    foreign key (plate_id) 
    references plate;

alter table assay_plate 
    add constraint fk_assay_plate_to_screen 
    foreign key (screen_id) 
    references screen;

alter table assay_plate 
    add constraint fk_assay_plate_to_library_screening 
    foreign key (library_screening_id) 
    references library_screening;

create sequence assay_plate_id_seq;

/* TODO: create sufficient rows to match: select max(number_of_replicates) from screening sa join library_screening ls using(activity_id) where not ls.is_for_screener_provided_plates; */
create temp table replicate_ordinal (replicate_ordinal int);
insert into replicate_ordinal (replicate_ordinal) values (0);
insert into replicate_ordinal (replicate_ordinal) values (1);
insert into replicate_ordinal (replicate_ordinal) values (2);
insert into replicate_ordinal (replicate_ordinal) values (3);
insert into replicate_ordinal (replicate_ordinal) values (4);
insert into replicate_ordinal (replicate_ordinal) values (5);
insert into replicate_ordinal (replicate_ordinal) values (6);
insert into replicate_ordinal (replicate_ordinal) values (7);
insert into replicate_ordinal (replicate_ordinal) values (8);
insert into replicate_ordinal (replicate_ordinal) values (9);
insert into replicate_ordinal (replicate_ordinal) values (10);
insert into replicate_ordinal (replicate_ordinal) values (11);
insert into replicate_ordinal (replicate_ordinal) values (12);
insert into replicate_ordinal (replicate_ordinal) values (13);
insert into replicate_ordinal (replicate_ordinal) values (14);
insert into replicate_ordinal (replicate_ordinal) values (15);
insert into replicate_ordinal (replicate_ordinal) values (16);
insert into replicate_ordinal (replicate_ordinal) values (17);
insert into replicate_ordinal (replicate_ordinal) values (18);
insert into replicate_ordinal (replicate_ordinal) values (19);

/* TODO: verify pre-condition: should = 0, to ensure assay_plate are created for plates screened:
select count(*) from screening s join library_screening ls using(activity_id) where not ls.is_for_screener_provided_plates and (number_of_replicates = 0 or number_of_replicates is null) and exists (select * from library_screening_plate_link lspl where lspl.library_screening_id = ls.activity_id);

to fix:
update screening set number_of_replicates = 1 from library_screening ls where ls.activity_id = screening.activity_id and not ls.is_for_screener_provided_plates and (number_of_replicates = 0 or number_of_replicates is null) and exists (select * from library_screening_plate_link lspl where lspl.library_screening_id = ls.activity_id);
*/

insert into assay_plate (assay_plate_id, screen_id, plate_id, plate_number, replicate_ordinal, library_screening_id, version)
select nextval('assay_plate_id_seq'), x.screen_id, x.plate_id, x.plate_number, x.replicate_ordinal, x.activity_id, 0
from (select la.screen_id, p.plate_id, p.plate_number, r.replicate_ordinal, ls.activity_id from library_screening_plate_link lspl join plate p using(plate_id) join library_screening ls on(ls.activity_id = lspl.library_screening_id) join screening sa using(activity_id) join lab_activity la using(activity_id) join replicate_ordinal r on (r.replicate_ordinal < sa.number_of_replicates)
where not ls.is_for_screener_provided_plates
order by screen_id, plate_number, replicate_ordinal) as x;


/* TODO: verify post-condition: counts of following 2 statements should be equal:
select sum(number_of_replicates) from screening s join library_screening_plate_link lspl on(activity_id = library_screening_id) join plate p using(plate_id);
select count(*) from assay_plate;
*/

drop table library_screening_plate_link;


COMMIT;
