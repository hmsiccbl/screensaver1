alter table cherry_pick drop constraint cherry_pick_to_cherry_pick_liquid_transfer;

alter table cherry_pick drop column cherry_pick_liquid_transfer_id;

create table cherry_pick_liquid_transfer_cherry_pick_link (
    cherry_pick_id int4 not null,
    cherry_pick_liquid_transfer_id int4 not null,
    primary key (cherry_pick_id, cherry_pick_liquid_transfer_id)
);


alter table cherry_pick_liquid_transfer_cherry_pick_link 
    add constraint FK1A47CCFBC5ED86E8 
    foreign key (cherry_pick_liquid_transfer_id) 
    references cherry_pick_liquid_transfer;

alter table cherry_pick_liquid_transfer_cherry_pick_link 
    add constraint FK1A47CCFBAFA809F0 
    foreign key (cherry_pick_id) 
    references cherry_pick;
