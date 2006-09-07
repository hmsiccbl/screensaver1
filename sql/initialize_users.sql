begin;

delete from role_user_link;
delete from screensaver_user_role;
delete from screensaver_user;

insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'developer', 'Special users that have permission to invoke development-related functionality and view low-level system information.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'readEverythingAdmin', 'Read-everything administrators will have the ability to view and search over data of all categories, except a screen\'s billing information. In addition, they will have the ability to generate various reports on screens.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'librariesAdmin', 'Administrators that can create and modify libraries.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'usersAdmin', 'Administrators that can create and modify user accounts.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'screensAdmin', 'Administrators that can create and modify screens.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'screenResultsAdmin', 'Administrators that can create and modify screen results.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'billingAdmin', 'Administrators that can view, create, and modify billing information for a screen.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'screeningRoomUser', 'Users that have permission to view and search over non-administrative information for certain data records.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'compoundScreeningRoomUser', 'Users that have permission to view and search over non-administrative information for all compound screens and any compound screen results which are demarked \'shareable\'.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'rnaiScreeningRoomUser', 'Users that have permission to view and search over non-administrative information for all RNAi screens.');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name, comments) values (nextval('screensaver_user_role_id_seq'), 1, 'medicinalChemistUser', 'Users that are medicinal chemists.');

/*
 * Need to generate a SHA1 hashed password?  Try: 
 *   perl -e 'use Digest::SHA1; my $sha1 = Digest::SHA1->new; $sha1->add("YOUR_PASSWORD"); print $sha1->hexdigest(), "\n";'
 */

/* guest has empty password */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Guest', 'guest@hms.harvard.edu', 'guest', 'da39a3ee5e6b4b0d3255bfef95601890afd80709');
insert into role_user_link (screensaver_user_id, screensaver_user_role_id) select currval('screensaver_user_id_seq'), screensaver_user_role_id from screensaver_user_role where role_name in ('screeningRoomUser', 'compoundScreeningRoomUser', 'rnaiScreeningRoomUser');

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Andrew', 'Tolopko', 'andrew_tolopko@hms.harvard.edu', 'ant', 'd015cc465bdb4e51987df7fb870472d3fb9a3505');
insert into role_user_link (screensaver_user_id, screensaver_user_role_id) select currval('screensaver_user_id_seq'), screensaver_user_role_id from screensaver_user_role where role_name in ('developer', 'readEverythingAdmin', 'usersAdmin', 'librariesAdmin', 'screenResultsAdmin');

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'john', 'sullivan', 'john_sullivan@hms.harvard.edu', 's', '102efc1a35e83cc91266bc8c6d34ea50c8eef0bc');
insert into role_user_link (screensaver_user_id, screensaver_user_role_id) select currval('screensaver_user_id_seq'), screensaver_user_role_id from screensaver_user_role where role_name in ('developer', 'readEverythingAdmin', 'usersAdmin', 'librariesAdmin', 'screenResultsAdmin');

commit;