begin;

delete from screensaver_user_role_type;
delete from screening_room_user;
delete from administrator_user;
delete from screensaver_user;

/*
 * Need to generate a SHA1 hashed password?  Try: 
 *   perl -e 'use Digest::SHA1; my $sha1 = Digest::SHA1->new; $sha1->add("YOUR_PASSWORD"); print $sha1->hexdigest(), "\n";'
 */

/* guest has empty password */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Guest', 'guest@hms.harvard.edu', 'guest', 'da39a3ee5e6b4b0d3255bfef95601890afd80709');
insert into screening_room_user (screensaver_user_id, user_classification, non_screening_user) values (currval('screensaver_user_id_seq'), 'ICCB-L/NSRB staff', false);
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screeningRoomUser';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'compoundScreeningRoomUser';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'rnaiScreeningRoomUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Andrew', 'Tolopko', 'andrew_tolopko@hms.harvard.edu', 'ant', 'd015cc465bdb4e51987df7fb870472d3fb9a3505', 'ant4');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'john', 'sullivan', 'john_sullivan@hms.harvard.edu', 's', '102efc1a35e83cc91266bc8c6d34ea50c8eef0bc', 'js163');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';


commit;