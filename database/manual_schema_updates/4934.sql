BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4934,
current_timestamp,
'added plate_location';

create table plate_location (
    plate_location_id int4 not null,
    bin text,
    freezer text,
    room text,
    shelf text,
    primary key (plate_location_id),
    unique (room, freezer, shelf, bin)
);


alter table plate add column plate_location_id int4;

alter table plate 
    add constraint fk_plate_to_plate_location 
    foreign key (plate_location_id) 
    references plate_location;

create sequence plate_location_id_seq;

/* TODO: as plate locations have been split into 4 parts (room,
freezer, shelf, bin), existing plate locations values will become the
'freezer' part of the expanded plate location, with the other parts
being set to null; change the update statement below, if different
migration strategy is needed */

insert into plate_location (plate_location_id, freezer) select distinct nextval('plate_location_id_seq'), location from (select distinct location from plate) as unique_locations;
update plate set plate_location_id = (select plate_location_id from plate_location pl where pl.freezer = plate.location);
alter table plate drop column location;
  
COMMIT;
