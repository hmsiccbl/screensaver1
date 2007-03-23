update screensaver_user set first_name='Nathan', ecommons_id=null, login='nmoerke', digested_password='d805a86e89cadb3f27357f75257ae737c7b800cc' where email='nathan_moerke@hms.harvard.edu';

drop table cherry_pick_request_empty_columns;

create table cherry_pick_request_requested_empty_columns (
    cherry_pick_request_id int4 not null,
    elt int4 not null,
    primary key (cherry_pick_request_id, elt)
);

alter table cherry_pick_request_requested_empty_columns 
    add constraint FKCB12A4F28E09CC35 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;
