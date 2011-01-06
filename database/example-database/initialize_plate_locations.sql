/* 
 * Initialize the plate locations.  These locations are assigned to Copy Plates. 
 */

begin;

/* Example attached file types for screens. */
insert into plate_location (plate_location_id, room, freezer, shelf, bin) values (nextval('plate_location_id_seq'), null, 'Freezer A', null, null);
insert into plate_location (plate_location_id, room, freezer, shelf, bin) values (nextval('plate_location_id_seq'), null, 'Freezer B', null, null);
insert into plate_location (plate_location_id, room, freezer, shelf, bin) values (nextval('plate_location_id_seq'), null, 'Freezer C', null, null);


commit;