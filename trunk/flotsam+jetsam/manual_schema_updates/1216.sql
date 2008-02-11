begin;

alter table screening_room_activity add column ordinal int4;

/* populate new ordinal column */
update screening_room_activity set ordinal = (select count(*)-1 from screening_room_activity sra2 where sra2.screen_id = sra1.screen_id and sra2.screening_room_activity_id <= sra1.screening_room_activity_id) from screening_room_activity sra1 where sra1.screening_room_activity_id = screening_room_activity.screening_room_activity_id;

alter table screening_room_activity alter column ordinal set not null;

commit;
