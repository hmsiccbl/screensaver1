begin;

alter table screening_room_activity 
    drop constraint fk_screening_room_activity_to_performed_by;

alter table screening_room_activity 
    add constraint fk_screening_room_activity_to_performed_by 
    foreign key (performed_by_id) 
    references screensaver_user;

alter table cherry_pick_liquid_transfer add column successful bool;
update cherry_pick_liquid_transfer set successful=true;
alter table cherry_pick_liquid_transfer alter column successful set not null;

commit;