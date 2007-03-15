alter table cherry_pick alter column copy_id drop not null;
alter table cherry_pick rename column destination_plate_type TO assay_plate_type;
alter table cherry_pick rename column destination_plate_name TO assay_plate_name;
alter table cherry_pick add column assay_plate_row int4;
alter table cherry_pick add column assay_plate_column int4;

alter table cherry_pick_request add column randomized_assay_plate_layout bool;
alter table cherry_pick_request alter column randomized_assay_plate_layout set not null;
alter table cherry_pick_request add column ordinal int4;
alter table cherry_pick_request alter column ordinal set not null;

create table cherry_pick_request_empty_columns (
    cherry_pick_request_id int4 not null,
    elt int4 not null,
    primary key (cherry_pick_request_id, elt)
);

alter table cherry_pick_request_empty_columns 
    add constraint FKCF5DD2038E09CC35 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select screensaver_user_id, 'cherryPickAdmin' from screensaver_user where email in ('andrew_tolopko@hms.harvard.edu', 'john_sullivan@hms.harvard.edu', 'stewart_rudnicki@hms.harvard.edu', 'Katrina_Schulberg@hms.harvard.edu', 'david_wrobel@hms.harvard.edu');

