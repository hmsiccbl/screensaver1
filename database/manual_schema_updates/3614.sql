BEGIN;

INSERT INTO schema_history(screensaver_revision, date_updated, comment)
SELECT
3614,
current_timestamp,
'migrate data sharing level info for screens and users';

update checklist_item set checklist_item_group = 'Legacy', order_statistic = order_statistic + (select max(order_statistic) from checklist_item where checklist_item_group = 'Legacy') where checklist_item_id in (20, 26, 27);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 1, true, 'Forms', 'Current Small Molecule User Agreement active'
);

update attached_file_type set value = '2009 ICCB-L/NSRB Small Molecule User Agreement' where value = 'ICCB-L/NSRB Small Molecule User Agreement';

update checklist_item set item_name = 'Current RNAi User Agreement active' where item_name = '2009 RNAi User agreement signed';


/* activate users' "Current Small Molecule User Agreement active" checklist items */

insert into checklist_item_event (checklist_item_event_id, checklist_item_id, screening_room_user_id, is_expiration, is_not_applicable, date_created, created_by_id, date_performed) 
select nextval('checklist_item_event_id_seq'), currval('checklist_item_id_seq'), screening_room_user_id, is_expiration, is_not_applicable, date_created, created_by_id, date_performed
from checklist_item_event cie where checklist_item_event_id in 
(select max(checklist_item_event_id) from checklist_item_event cie2 where
cie.screening_room_user_id=cie2.screening_room_user_id and cie2.checklist_item_id in
(20, 26, 27)) and not cie.is_expiration;


/* set user SM DSL roles based upon SM UA checklist items */  

/* this delete/insert shouldn't be necessary if roles were correct in
extant data; this should recreate things as they were */
delete from screensaver_user_role where screensaver_user_role = 'smallMoleculeScreener';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role)
select screening_room_user_id, 'smallMoleculeScreener'
from checklist_item_event cie where checklist_item_event_id in 
(select max(checklist_item_event_id) from checklist_item_event cie2 where
cie.screening_room_user_id=cie2.screening_room_user_id and cie2.checklist_item_id in
(20, 26, 27)) and not cie.is_expiration;

/*  this delete shouldn't be necessary, since role did not previously exist */
delete from screensaver_user_role where screensaver_user_role = 'smDsl2MutualPositives'; 
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role)
select screening_room_user_id, 'smDsl2MutualPositives'
from checklist_item_event cie where checklist_item_event_id in 
(select max(checklist_item_event_id) from checklist_item_event cie2 where
cie.screening_room_user_id=cie2.screening_room_user_id and cie2.checklist_item_id in
(20, 26)) and not cie.is_expiration;

/*  this delete shouldn't be necessary, since role did not previously exist */
delete from screensaver_user_role where screensaver_user_role = 'smDsl1MutualScreens';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role)
select screening_room_user_id, 'smDsl1MutualScreens'
from checklist_item_event cie where checklist_item_event_id in 
(select max(checklist_item_event_id) from checklist_item_event cie2 where
cie.screening_room_user_id=cie2.screening_room_user_id and cie2.checklist_item_id in
(20)) and not cie.is_expiration;

delete from screensaver_user_role where screensaver_user_role = 'screener';


/* set screens' DSLs */

/* set to invalid DSL value, to ensure we consider all screens */
update screen set data_sharing_level = 999 where screen_type = 'Small Molecule';

/* Cat1: Screens with data sharing levels 2 or 3 and data has not been deposited since Jan. 8, 2008 (two years).  Thus the data for these screens should be shared.  The data sharing date will be listed as two years after "last date data returned to ICCBL" date, column I. Data sharing comment for all of these screens should be "Data designated as shared on 1/8/10 by Caroline Shamu because last data deposition occurred before January 2008". Comments for each screen will also indicate the actual date of last data returned to ICCBL. */
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/25/2004' + interval '2 years' where screen_number = 144;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/1/2004' + interval '2 years' where screen_number = 126;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/13/2004' + interval '2 years' where screen_number = 145;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/13/2004' + interval '2 years' where screen_number = 210;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/30/2004' + interval '2 years' where screen_number = 213;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/5/2004' + interval '2 years' where screen_number = 330;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/4/2004' + interval '2 years' where screen_number = 324;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/2/2004' + interval '2 years' where screen_number = 120;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/7/2005' + interval '2 years' where screen_number = 118;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/16/2005' + interval '2 years' where screen_number = 119;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 149;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 325;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/22/2005' + interval '2 years' where screen_number = 136;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/7/2005' + interval '2 years' where screen_number = 475;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/3/2005' + interval '2 years' where screen_number = 430;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/13/2005' + interval '2 years' where screen_number = 510;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/14/2005' + interval '2 years' where screen_number = 131;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/22/2005' + interval '2 years' where screen_number = 514;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/6/2005' + interval '2 years' where screen_number = 473;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/23/2005' + interval '2 years' where screen_number = 538;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/28/2006' + interval '2 years' where screen_number = 389;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/13/2006' + interval '2 years' where screen_number = 546;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/13/2006' + interval '2 years' where screen_number = 651;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/6/2006' + interval '2 years' where screen_number = 468;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/1/2006' + interval '2 years' where screen_number = 635;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/20/2006' + interval '2 years' where screen_number = 642;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/13/2007' + interval '2 years' where screen_number = 415;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/11/2007' + interval '2 years' where screen_number = 500;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/11/2007' + interval '2 years' where screen_number = 688;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/31/2007' + interval '2 years' where screen_number = 654;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/5/2007' + interval '2 years' where screen_number = 547;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/5/2007' + interval '2 years' where screen_number = 686;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/12/2007' + interval '2 years' where screen_number = 630;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/12/2007' + interval '2 years' where screen_number = 670;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/19/2007' + interval '2 years' where screen_number = 632;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/6/2007' + interval '2 years' where screen_number = 640;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/21/2007' + interval '2 years' where screen_number = 550;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/30/2007' + interval '2 years' where screen_number = 695;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/13/2007' + interval '2 years' where screen_number = 367;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/13/2007' + interval '2 years' where screen_number = 692;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/13/2007' + interval '2 years' where screen_number = 723;

insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 144;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 144;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 126;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 126;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 145;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 145;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 210;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 210;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 213;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 213;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 330;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 330;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 324;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 324;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 120;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 120;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 118;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 118;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 119;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 119;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 149;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 149;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 325;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 325;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 136;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 136;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 475;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 475;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 430;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 430;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 510;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 510;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 131;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 131;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 514;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 514;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 473;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 473;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 538;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 538;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 389;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 389;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 546;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 546;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 651;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 651;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 468;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 468;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 635;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 635;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 642;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 642;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 415;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 415;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 500;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 500;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 688;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 688;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 654;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 654;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 547;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 547;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 686;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 686;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 630;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 630;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 670;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 670;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 632;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 632;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 640;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 640;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 550;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 550;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 695;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 695;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 367;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 367;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 692;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 692;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 723;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 723;



/* Cat3: 01/08/2010 - Screens with no data sharing agreement and data has not been deposited since Jan. 8, 2008 (two years).  Thus the data for these screens should be shared.  The data sharing date will be listed as two years after "last date data returned to ICCBL" date, column I. Data sharing comment for all of these screens should be "Data designated as shared on 01/08/2010 by Caroline Shamu because last data deposition occurred before January 2008". Comments for each screen will also indicate the actual date of last data returned to ICCBL.  
*/
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/5/2001' + interval '2 years' where screen_number = 264;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/17/2002' + interval '2 years' where screen_number = 230;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/24/2003' + interval '2 years' where screen_number = 261;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/30/2004' + interval '2 years' where screen_number = 141;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/9/2004' + interval '2 years' where screen_number = 148;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/12/2004' + interval '2 years' where screen_number = 124;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/13/2004' + interval '2 years' where screen_number = 186;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/14/2004' + interval '2 years' where screen_number = 140;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/15/2004' + interval '2 years' where screen_number = 191;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/16/2004' + interval '2 years' where screen_number = 183;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/16/2004' + interval '2 years' where screen_number = 225;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/30/2004' + interval '2 years' where screen_number = 310;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/3/2004' + interval '2 years' where screen_number = 222;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/4/2004' + interval '2 years' where screen_number = 184;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/13/2004' + interval '2 years' where screen_number = 285;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/22/2004' + interval '2 years' where screen_number = 309;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/28/2004' + interval '2 years' where screen_number = 368;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/2/2004' + interval '2 years' where screen_number = 142;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/2/2004' + interval '2 years' where screen_number = 269;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/4/2004' + interval '2 years' where screen_number = 370;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/4/2004' + interval '2 years' where screen_number = 402;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/5/2004' + interval '2 years' where screen_number = 268;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/5/2004' + interval '2 years' where screen_number = 311;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/5/2004' + interval '2 years' where screen_number = 322;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/5/2004' + interval '2 years' where screen_number = 333;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/12/2004' + interval '2 years' where screen_number = 383;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/19/2004' + interval '2 years' where screen_number = 331;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/30/2004' + interval '2 years' where screen_number = 271;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/6/2004' + interval '2 years' where screen_number = 284;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/7/2004' + interval '2 years' where screen_number = 302;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/7/2004' + interval '2 years' where screen_number = 303;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/15/2004' + interval '2 years' where screen_number = 342;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/3/2005' + interval '2 years' where screen_number = 229;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/3/2005' + interval '2 years' where screen_number = 295;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/20/2005' + interval '2 years' where screen_number = 315;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/31/2005' + interval '2 years' where screen_number = 143;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/31/2005' + interval '2 years' where screen_number = 272;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/1/2005' + interval '2 years' where screen_number = 304;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/7/2005' + interval '2 years' where screen_number = 455;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/14/2005' + interval '2 years' where screen_number = 417;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 130;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 189;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 190;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 218;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 219;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 257;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/17/2005' + interval '2 years' where screen_number = 289;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/18/2005' + interval '2 years' where screen_number = 263;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/22/2005' + interval '2 years' where screen_number = 297;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/23/2005' + interval '2 years' where screen_number = 345;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/23/2005' + interval '2 years' where screen_number = 379;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/24/2005' + interval '2 years' where screen_number = 197;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/24/2005' + interval '2 years' where screen_number = 398;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/16/2005' + interval '2 years' where screen_number = 327;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/23/2005' + interval '2 years' where screen_number = 413;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/1/2005' + interval '2 years' where screen_number = 393;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/5/2005' + interval '2 years' where screen_number = 505;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/29/2005' + interval '2 years' where screen_number = 108;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/29/2005' + interval '2 years' where screen_number = 115;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/29/2005' + interval '2 years' where screen_number = 116;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/5/2005' + interval '2 years' where screen_number = 401;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/5/2005' + interval '2 years' where screen_number = 460;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/5/2005' + interval '2 years' where screen_number = 512;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/11/2005' + interval '2 years' where screen_number = 469;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/4/2005' + interval '2 years' where screen_number = 476;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/9/2005' + interval '2 years' where screen_number = 431;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/30/2005' + interval '2 years' where screen_number = 499;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/17/2005' + interval '2 years' where screen_number = 412;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/2/2005' + interval '2 years' where screen_number = 343;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/20/2005' + interval '2 years' where screen_number = 123;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/24/2006' + interval '2 years' where screen_number = 477;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '1/24/2006' + interval '2 years' where screen_number = 534;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/22/2006' + interval '2 years' where screen_number = 518;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/3/2006' + interval '2 years' where screen_number = 517;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/2/2006' + interval '2 years' where screen_number = 539;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/28/2006' + interval '2 years' where screen_number = 508;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/3/2006' + interval '2 years' where screen_number = 543;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/25/2006' + interval '2 years' where screen_number = 516;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '5/25/2006' + interval '2 years' where screen_number = 584;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/29/2006' + interval '2 years' where screen_number = 545;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/11/2006' + interval '2 years' where screen_number = 690;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/13/2006' + interval '2 years' where screen_number = 548;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/18/2006' + interval '2 years' where screen_number = 472;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/24/2006' + interval '2 years' where screen_number = 563;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/8/2006' + interval '2 years' where screen_number = 531;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/20/2006' + interval '2 years' where screen_number = 605;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/29/2006' + interval '2 years' where screen_number = 641;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/9/2006' + interval '2 years' where screen_number = 537;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/21/2006' + interval '2 years' where screen_number = 511;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/20/2006' + interval '2 years' where screen_number = 662;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/23/2007' + interval '2 years' where screen_number = 634;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '2/24/2007' + interval '2 years' where screen_number = 645;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '3/2/2007' + interval '2 years' where screen_number = 672;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/5/2007' + interval '2 years' where screen_number = 428;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/12/2007' + interval '2 years' where screen_number = 403;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/12/2007' + interval '2 years' where screen_number = 668;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/5/2007' + interval '2 years' where screen_number = 661;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/7/2007' + interval '2 years' where screen_number = 706;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/8/2007' + interval '2 years' where screen_number = 459;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '6/18/2007' + interval '2 years' where screen_number = 657;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/18/2007' + interval '2 years' where screen_number = 464;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '7/19/2007' + interval '2 years' where screen_number = 501;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/10/2007' + interval '2 years' where screen_number = 697;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/20/2007' + interval '2 years' where screen_number = 667;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/4/2007' + interval '2 years' where screen_number = 689;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '9/27/2007' + interval '2 years' where screen_number = 673;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '10/26/2007' + interval '2 years' where screen_number = 708;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '11/20/2007' + interval '2 years' where screen_number = 731;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '12/5/2007' + interval '2 years' where screen_number = 449;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '4/5/2007' + interval '2 years' where screen_number = 691;
update screen set data_sharing_level = 1, data_privacy_expiration_date = date '8/8/2006' + interval '2 years' where screen_number = 699;

insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 264;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 264;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 230;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 230;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 261;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 261;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 141;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 141;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 148;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 148;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 124;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 124;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 186;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 186;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 140;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 140;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 191;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 191;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 183;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 183;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 225;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 225;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 310;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 310;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 222;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 222;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 184;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 184;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 285;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 285;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 309;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 309;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 368;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 368;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 142;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 142;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 269;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 269;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 370;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 370;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 402;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 402;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 268;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 268;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 311;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 311;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 322;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 322;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 333;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 333;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 383;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 383;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 331;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 331;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 271;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 271;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 284;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 284;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 302;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 302;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 303;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 303;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 342;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 342;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 229;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 229;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 295;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 295;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 315;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 315;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 143;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 143;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 272;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 272;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 304;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 304;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 455;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 455;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 417;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 417;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 130;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 130;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 189;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 189;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 190;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 190;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 218;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 218;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 219;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 219;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 257;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 257;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 289;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 289;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 263;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 263;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 297;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 297;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 345;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 345;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 379;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 379;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 197;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 197;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 398;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 398;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 327;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 327;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 413;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 413;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 393;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 393;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 505;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 505;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 108;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 108;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 115;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 115;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 116;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 116;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 401;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 401;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 460;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 460;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 512;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 512;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 469;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 469;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 476;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 476;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 431;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 431;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 499;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 499;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 412;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 412;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 343;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 343;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 123;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 123;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 477;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 477;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 534;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 534;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 518;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 518;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 517;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 517;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 539;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 539;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 508;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 508;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 543;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 543;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 516;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 516;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 584;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 584;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 545;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 545;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 690;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 690;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 548;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 548;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 472;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 472;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 563;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 563;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 531;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 531;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 605;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 605;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 641;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 641;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 537;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 537;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 511;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 511;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 662;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 662;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 634;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 634;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 645;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 645;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 672;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 672;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 428;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 428;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 403;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 403;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 668;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 668;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 661;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 661;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 706;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 706;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 459;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 459;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 657;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 657;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 464;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 464;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 501;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 501;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 697;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 697;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 667;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 667;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 689;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 689;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 673;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 673;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 708;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 708;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 731;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 731;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 449;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 449;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 691;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 691;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 1. Data designated as mutually shared (level 1) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred before January 2008.'
from screen where screen_number = 699;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 699;


/* Cat2: 1/8/10- Screens with data sharing levels 2 or 3 and data was deposited after Jan. 8, 2008 (within the last two years).  Thus the data for these screens should NOT be shared.  The data sharing date will be listed as two years after "last date data returned to ICCBL" date, column I. Comments for each screen will also indicate the actual date of last data returned to ICCBL.  
*/

update screen set data_privacy_expiration_date = date '2/1/2008' + interval '2 years' where screen_number = 764;
update screen set data_privacy_expiration_date = date '2/21/2008' + interval '2 years' where screen_number = 603;
update screen set data_privacy_expiration_date = date '3/24/2008' + interval '2 years' where screen_number = 750;
update screen set data_privacy_expiration_date = date '4/9/2008' + interval '2 years' where screen_number = 725;
update screen set data_privacy_expiration_date = date '4/22/2008' + interval '2 years' where screen_number = 777;
update screen set data_privacy_expiration_date = date '4/25/2008' + interval '2 years' where screen_number = 658;
update screen set data_privacy_expiration_date = date '4/25/2008' + interval '2 years' where screen_number = 751;
update screen set data_privacy_expiration_date = date '5/15/2008' + interval '2 years' where screen_number = 329;
update screen set data_privacy_expiration_date = date '5/23/2008' + interval '2 years' where screen_number = 804;
update screen set data_privacy_expiration_date = date '6/24/2008' + interval '2 years' where screen_number = 816;
update screen set data_privacy_expiration_date = date '7/28/2008' + interval '2 years' where screen_number = 782;
update screen set data_privacy_expiration_date = date '8/28/2008' + interval '2 years' where screen_number = 817;
update screen set data_privacy_expiration_date = date '9/16/2008' + interval '2 years' where screen_number = 840;
update screen set data_privacy_expiration_date = date '9/30/2008' + interval '2 years' where screen_number = 827;
update screen set data_privacy_expiration_date = date '10/7/2008' + interval '2 years' where screen_number = 833;
update screen set data_privacy_expiration_date = date '11/20/2008' + interval '2 years' where screen_number = 755;
update screen set data_privacy_expiration_date = date '11/24/2008' + interval '2 years' where screen_number = 861;
update screen set data_privacy_expiration_date = date '12/5/2008' + interval '2 years' where screen_number = 836;
update screen set data_privacy_expiration_date = date '12/15/2008' + interval '2 years' where screen_number = 772;
update screen set data_privacy_expiration_date = date '12/22/2008' + interval '2 years' where screen_number = 682;
update screen set data_privacy_expiration_date = date '1/9/2009' + interval '2 years' where screen_number = 834;
update screen set data_privacy_expiration_date = date '1/22/2009' + interval '2 years' where screen_number = 823;
update screen set data_privacy_expiration_date = date '12/15/2009' + interval '2 years' where screen_number = 506;
update screen set data_privacy_expiration_date = date '4/2/2009' + interval '2 years' where screen_number = 776;
update screen set data_privacy_expiration_date = date '4/2/2009' + interval '2 years' where screen_number = 783;
update screen set data_privacy_expiration_date = date '4/20/2009' + interval '2 years' where screen_number = 853;
update screen set data_privacy_expiration_date = date '5/7/2009' + interval '2 years' where screen_number = 685;
update screen set data_privacy_expiration_date = date '5/26/2009' + interval '2 years' where screen_number = 897;
update screen set data_privacy_expiration_date = date '6/3/2009' + interval '2 years' where screen_number = 656;
update screen set data_privacy_expiration_date = date '6/16/2009' + interval '2 years' where screen_number = 803;
update screen set data_privacy_expiration_date = date '7/15/2009' + interval '2 years' where screen_number = 873;
update screen set data_privacy_expiration_date = date '11/10/2009' + interval '2 years' where screen_number = 421;
update screen set data_privacy_expiration_date = date '8/10/2009' + interval '2 years' where screen_number = 884;
update screen set data_privacy_expiration_date = date '8/12/2009' + interval '2 years' where screen_number = 905;
update screen set data_privacy_expiration_date = date '8/15/2009' + interval '2 years' where screen_number = 901;
update screen set data_privacy_expiration_date = date '8/21/2009' + interval '2 years' where screen_number = 529;
update screen set data_privacy_expiration_date = date '8/21/2009' + interval '2 years' where screen_number = 756;
update screen set data_privacy_expiration_date = date '9/3/2009' + interval '2 years' where screen_number = 918;
update screen set data_privacy_expiration_date = date '10/1/2009' + interval '2 years' where screen_number = 915;
update screen set data_privacy_expiration_date = date '10/1/2009' + interval '2 years' where screen_number = 921;
update screen set data_privacy_expiration_date = date '10/21/2009' + interval '2 years' where screen_number = 933;
update screen set data_privacy_expiration_date = date '10/25/2009' + interval '2 years' where screen_number = 769;
update screen set data_privacy_expiration_date = date '10/27/2009' + interval '2 years' where screen_number = 678;
update screen set data_privacy_expiration_date = date '10/27/2009' + interval '2 years' where screen_number = 922;
update screen set data_privacy_expiration_date = date '10/27/2009' + interval '2 years' where screen_number = 940;
update screen set data_privacy_expiration_date = date '10/29/2009' + interval '2 years' where screen_number = 614;
update screen set data_privacy_expiration_date = date '10/29/2009' + interval '2 years' where screen_number = 908;
update screen set data_privacy_expiration_date = date '10/29/2009' + interval '2 years' where screen_number = 909;
update screen set data_privacy_expiration_date = date '11/9/2009' + interval '2 years' where screen_number = 919;
update screen set data_privacy_expiration_date = date '5/29/2008' + interval '2 years' where screen_number = 771;
update screen set data_privacy_expiration_date = date '11/10/2008' + interval '2 years' where screen_number = 846;
update screen set data_privacy_expiration_date = date '11/10/2008' + interval '2 years' where screen_number = 753;
update screen set data_privacy_expiration_date = date '7/28/2008' + interval '2 years' where screen_number = 831;
/*update screen set data_privacy_expiration_date = date '4/8/2008' + interval '2 years' where screen_number = 741;*/
/*update screen set data_privacy_expiration_date = date '7/23/2008' + interval '2 years' where screen_number = 659;*/
/*update screen set data_privacy_expiration_date = date '11/26/2008' + interval '2 years' where screen_number = 781;*/
/*update screen set data_privacy_expiration_date = date '10/27/2009' + interval '2 years' where screen_number = 775;*/

/* [#2204] */
update screen set lab_head_id = 251 where screen_number = 831; 
insert into collaborator_link (screen_id, collaborator_id) values (1417, 124);

insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 764;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 764;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 603;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 603;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 750;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 750;
/*
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 741;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 741;
*/
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 725;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 725;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 777;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 777;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 658;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 658;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 751;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 751;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 329;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 329;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 804;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 804;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 816;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 816;
/*
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 659;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 659;
*/
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 782;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 782;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 817;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 817;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 840;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 840;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 827;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 827;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 833;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 833;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 755;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 755;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 861;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 861;
/*
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 781;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 781;
*/
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 836;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 836;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 772;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 772;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 682;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 682;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 834;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 834;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 823;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 823;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 506;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 506;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 776;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 776;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 783;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 783;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 853;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 853;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 685;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 685;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 897;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 897;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 656;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 656;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 803;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 803;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 873;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 873;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 421;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 421;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 884;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 884;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 905;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 905;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 901;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 901;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 529;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 529;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 756;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 756;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 918;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 918;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 915;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 915;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 921;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 921;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 933;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 933;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 769;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 769;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 678;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 678;
/*
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 775;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 775;
*/
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 922;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 922;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 940;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 940;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 614;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 614;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 908;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 908;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 909;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 909;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 919;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 919;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 771;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 771;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 846;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 846;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 753;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 753;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 831;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 831;

/* Cat4: 1/8/10- Screens with no DSL and data was deposited after Jan. 8, 2008 (within the last two years).  Thus the data for these screens should NOT be shared.  The data sharing date will be listed as two years after "last date data returned to ICCBL" date, column I. Comments for each screen will also indicate the actual date of last data returned to ICCBL.  */

update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/18/2008' + interval '2 years' where screen_number = 728;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '4/7/2008' + interval '2 years' where screen_number = 714;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '6/11/2008' + interval '2 years' where screen_number = 765;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '10/8/2008' + interval '2 years' where screen_number = 308;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '10/24/2008' + interval '2 years' where screen_number = 665;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/6/2009' + interval '2 years' where screen_number = 727;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/23/2009' + interval '2 years' where screen_number = 842;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '2/26/2009' + interval '2 years' where screen_number = 825;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '4/29/2009' + interval '2 years' where screen_number = 463;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '5/19/2009' + interval '2 years' where screen_number = 802;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '5/19/2009' + interval '2 years' where screen_number = 879;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '5/19/2009' + interval '2 years' where screen_number = 882;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '5/31/2009' + interval '2 years' where screen_number = 705;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '7/10/2009' + interval '2 years' where screen_number = 676;

insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 825;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 825;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 765;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 765;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 802;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 802;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 705;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 705;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 665;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 665;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 714;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 714;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 676;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 676;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 463;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 463;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 842;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 842;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 728;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 728;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 727;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 727;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 882;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 882;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 879;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 879;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set screen data sharing level to 3. Data designated as private (level 3) on 01/08/2010 by Caroline Shamu because last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ') occurred after January 2008 and PI had not yet signed data sharing agreement'
from screen where screen_number = 308;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 308;


/* Cat5: 01/08/2010 - these are all old screens from the Schreiber lab with no data sharing agreement.  Caroline has determined that the data should NOT be shared and permanently listed as level 3 (private).  The comment for each screen should be "1/8/10 the data for these screens is designated NOT shared as determined by Caroline Shamu" */
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 128;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 252;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 319;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 320;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 321;
update screen set data_sharing_level = 3, data_privacy_expiration_date = date '1/1/2999' where screen_number = 471;

insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 128;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 128;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 252;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 252;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 319;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 319;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 320;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 320;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 321;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 321;
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select nextval('activity_id_seq'), 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data privacy expiration date to ' || to_char(data_privacy_expiration_date, 'MM/DD/YYYY') || ' from date of last data deposition (' || to_char(data_privacy_expiration_date - interval '2 years', 'MM/DD/YYYY') || ')'
from screen where screen_number = 471;
insert into administrative_activity (activity_id, administrative_activity_type) values (currval('activity_id_seq'), 'Entity Update');
insert into screen_update_activity (update_activity_id, screen_id) select currval('activity_id_seq'), screen_id from screen where screen_number = 471;

/* make studies public */

update screen set data_sharing_level = 0 where screen_number >= 100000;


/* default screen DSL cases */

/*savepoint predefaultdsl;*/

update screen set data_sharing_level = -1
from screensaver_user_role r 
where r.screensaver_user_id = screen.lab_head_id and
r.screensaver_user_role = 'smDsl1MutualScreens' and
screen_type = 'Small Molecule' and data_sharing_level = 999;

update screen set data_sharing_level = -2
from screensaver_user_role r 
where r.screensaver_user_id = screen.lab_head_id and
r.screensaver_user_role = 'smDsl2MutualPositives' and
screen_type = 'Small Molecule' and data_sharing_level = 999;

update screen set data_sharing_level = -3
where screen_type = 'Small Molecule' and data_sharing_level = 999;

alter table screen_update_activity drop constraint FK8D9B1810ACAEF6AE;
ALTER TABLE administrative_activity drop constraint FKF20C1700702E36D6;
insert into screen_update_activity (update_activity_id, screen_id) 
select nextval('activity_id_seq'), screen_id 
from screen where data_sharing_level < 0;
insert into administrative_activity (activity_id, administrative_activity_type) select update_activity_id, 'Entity Update' from screen_update_activity sua where not exists (select * from administrative_activity where activity_id = sua.update_activity_id);
insert into activity (activity_id, version, date_created, performed_by_id, date_of_activity, created_by_id, comments)
select activity_id, 1, now(), 757, '2010-01-08', 755, '1.9.0 data migration: set data sharing level to ' || (s.data_sharing_level * -1)
from administrative_activity aa join screen_update_activity sua on(sua.update_activity_id=aa.activity_id) join screen s using(screen_id) where not exists (select * from activity where activity_id = aa.activity_id);
alter table screen_update_activity 
    add constraint FK8D9B1810ACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table administrative_activity 
    add constraint FKF20C1700702E36D6 
    foreign key (activity_id) 
    references activity;

update screen set data_sharing_level = data_sharing_level * -1 where data_sharing_level < 0;

update screen set data_sharing_level = 3 where screen_type = 'RNAi';

/* respect screen_result.is_shareable flag, which was used at ICCB-L to record published/public results */
update screen set data_sharing_level = 0 from screen_result sr
where sr.screen_id = screen.screen_id and sr.is_shareable and (screen_type = 'RNAi' or data_sharing_level = 999);
/* drop screen results shareable flag, now that we've transferred the previous intention of the field to the screen.data_sharing_level */
alter table screen_result drop is_shareable;


/* ASSERT: all SM screens w/data and DSL 2 or 3 have a data privacy expiration date */
\pset format unaligned
\pset fieldsep ','
\o screens-missing-data-privacy-expiration-date.csv
select screen_number as "screens missing data privacy expiration date" 
from screen s join screen_result using(screen_id) 
where s.data_sharing_level in (2,3) and data_privacy_expiration_date is null 
and screen_type = 'Small Molecule';
\o

/* ASSERT: all screens have been assigned a DSL value */
\pset format unaligned
\pset fieldsep ','
\o screens-with-unset-dsl.csv
select screen_number as "screens with unset DSL" from screen s where s.data_sharing_level = 999;
\o

/* add userRolesAdmin role */

insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) values (763, 'userRolesAdmin');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) values (757, 'userRolesAdmin');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) values (765, 'userRolesAdmin');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) values (3444, 'userRolesAdmin');

/* update user facility roles (SM screener, RNAi screener) based upon
existence of relationship with respective screen types */

delete from screening_room_user_facility_usage_role where facility_usage_role in ('smallMoleculeScreener', 'rnaiScreener');

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

ANALYZE;
