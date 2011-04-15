BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5190,
current_timestamp,
'many-to-many relationship for cherry_pick_assay_plate and cherry_pick_screening';

create table cherry_pick_assay_plate_screening_link (
    cherry_pick_assay_plate_id int4 not null,
    cherry_pick_screening_id int4 not null,
    primary key (cherry_pick_assay_plate_id, cherry_pick_screening_id)
);

insert into cherry_pick_assay_plate_screening_link (cherry_pick_assay_plate_id, cherry_pick_screening_id) 
select cherry_pick_assay_plate_id, cherry_pick_screening_id from cherry_pick_assay_plate where cherry_pick_screening_id is not null;

alter table cherry_pick_assay_plate drop column cherry_pick_screening_id;

alter table cherry_pick_assay_plate_screening_link 
    add constraint FKB6B905448458B415 
    foreign key (cherry_pick_screening_id) 
    references cherry_pick_screening;

alter table cherry_pick_assay_plate_screening_link 
    add constraint FKB6B90544F11EC30A 
    foreign key (cherry_pick_assay_plate_id) 
    references cherry_pick_assay_plate;


COMMIT;