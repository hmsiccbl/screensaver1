BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4026,
current_timestamp,
'plates_used replaced by plate-to-library_screening relationship';

create table library_screening_plate_link (
    library_screening_id int4 not null,
    plate_id int4 not null
);

alter table library_screening_plate_link 
    add constraint FKF037C470AC7FFB69 
    foreign key (plate_id) 
    references plate;

alter table library_screening_plate_link 
    add constraint FKF037C470836F0186 
    foreign key (library_screening_id) 
    references library_screening;

/* create missing copies and plates (i.e., copies and plates that are referenced by library screenings), but that were never explicitly created */

insert into copy (copy_id, name, usage_type, version, library_id) 
select l.short_name || ':' || pu.copy_name, pu.copy_name, 'For Library Screening', 0, l.library_id
from plates_used pu join library l on(pu.start_plate between l.start_plate and l.end_plate or pu.end_plate between l.start_plate and l.end_plate)
where not exists (select * from copy c where c.library_id = l.library_id and c.name = pu.copy_name)
group by l.short_name, l.library_id, pu.copy_name;

/* TODO: verify pre-condition: result size should be 0, indicating all libraries have wells defined 
select l.short_name, start_plate, end_plate from library l where not exists (select * from well where library_id = l.library_id);

to fix: run edu.harvard.med.iccbl.screensaver.io.libraries.CreateLibraryWells for each offending library
*/

insert into plate (plate_id, comments, location, plate_number, plate_type, well_volume, copy_id, version) 
select nextval('plate_id_seq'), 
'created automatically during database migration (plate type and initial well volume need to be set to correct values)', 
'', w.plate_number, 'ABGene',  0.0, c.copy_id, 0
from copy c join well w using(library_id)
where not exists (select * from plate p where p.plate_number = w.plate_number and p.copy_id = c.copy_id)
group by c.copy_id, w.plate_number;


/* replace plates_used with relationship between library_screening and plate */

insert into library_screening_plate_link (library_screening_id, plate_id) 
select library_screening_id, plate_id 
from plates_used pu,
library l join copy c using(library_id) join plate p using(copy_id)
where (p.plate_number between pu.start_plate and pu.end_plate or p.plate_number between pu.end_plate and pu.start_plate) 
and c.name = pu.copy_name
group by library_screening_id, plate_id;

/* TODO: verify post-condition: result size should be 0, indicating that library screenings maintained the
number of distinct plates screened 
select library_screening_id, sum(abs(pu.end_plate - pu.start_plate) + 1) as old_plate_count, (select count(*) from library_screening_plate_link lspl where lspl.library_screening_id = pu.library_screening_id) as new_plate_count from plates_used pu group by library_screening_id having sum(abs(pu.end_plate - pu.start_plate) + 1) <> (select count(*) from library_screening_plate_link lspl where lspl.library_screening_id = pu.library_screening_id);
*/

drop table plates_used;
drop sequence plates_used_id_seq;

COMMIT;
