BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3585,
current_timestamp,
'move some user roles to facility usage role';

create table screening_room_user_facility_usage_role (
    screening_room_user_id int4 not null,
    facility_usage_role text not null,
    primary key (screening_room_user_id, facility_usage_role)
);

alter table screening_room_user_facility_usage_role
    add constraint FK35C212BE4A89084E 
    foreign key (screening_room_user_id) 
    references screening_room_user;

insert into screening_room_user_facility_usage_role (screening_room_user_id, facility_usage_role) 
select screensaver_user_id, screensaver_user_role
from screensaver_user_role where screensaver_user_role in ('medicinalChemistUser', 'nonScreeningUser', 'qpcrUser');

delete from screensaver_user_role where screensaver_user_role in ('medicinalChemistUser', 'nonScreeningUser', 'qpcrUser');

/* update users' facility roles "smallMoleculeScreener" and "rnaiScreener" based upon
existence of relationship with respective screen types */

insert into screening_room_user_facility_usage_role (screening_room_user_id, facility_usage_role)
select screensaver_user_id, 'smallMoleculeScreener' from screening_room_user sru where 
exists (select * from screen where lab_head_id = sru.screensaver_user_id and screen_type = 'Small Molecule')
or exists (select * from screen where lead_screener_id = sru.screensaver_user_id and screen_type = 'Small Molecule')
or exists (select * from screen s join collaborator_link cl using(screen_id) where collaborator_id = sru.screensaver_user_id and screen_type = 'Small Molecule');

insert into screening_room_user_facility_usage_role (screening_room_user_id, facility_usage_role)
select screensaver_user_id, 'rnaiScreener' from screening_room_user sru where 
exists (select * from screen where lab_head_id = sru.screensaver_user_id and screen_type = 'RNAi')
or exists (select * from screen where lead_screener_id = sru.screensaver_user_id and screen_type = 'RNAi')
or exists (select * from screen s join collaborator_link cl using(screen_id) 
where collaborator_id = sru.screensaver_user_id and screen_type = 'RNAi');

COMMIT;
