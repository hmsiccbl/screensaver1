BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4978,
current_timestamp,
'make plate_location fields non-nullable, using empty string instead of null';

alter table plate_location drop constraint plate_location_room_key;

update plate_location set room = '<not specified>' where room is null or room = '';
update plate_location set freezer = '<not specified>' where freezer is null or freezer = '';
update plate_location set shelf = '<not specified>' where shelf is null or shelf = '';
update plate_location set bin = '<not specified>' where bin is null or bin = '';

update plate set plate_location_id = (select min(plate_location_id) from plate_location pl2 where pl2.room = pl1.room and pl2.freezer = pl1.freezer and pl2.shelf = pl1.shelf and pl2.bin = pl2.bin) 
from plate_location pl1 where pl1.plate_location_id = plate.plate_location_id;

delete from plate_location where not exists (select * from plate where plate_location_id = plate_location.plate_location_id);

alter table plate_location alter column room set not null;
alter table plate_location alter column freezer set not null;
alter table plate_location alter column shelf set not null;
alter table plate_location alter column bin set not null;

alter table plate_location add constraint plate_location_room_key unique (room, freezer, shelf, bin); 

COMMIT;
