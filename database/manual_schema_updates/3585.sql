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
from screensaver_user_role where screensaver_user_role in ('medicinalChemistUser', 'nonScreeningUser', 'qpcrUser', 'rnaiScreener', 'smallMoleculeScreener');

delete from screensaver_user_role where screensaver_user_role in ('medicinalChemistUser', 'nonScreeningUser', 'qpcrUser' /*, 'rnaiScreener', 'smallMoleculeScreener'*/);

COMMIT;
