begin;

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

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Caroline', 'Shamu', 'caroline_shamu@hms.harvard.edu', 'cshamu', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Rushir', 'Shaw', 'rushir_shaw@hms.harvard.edu', 'rshah', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Brian', 'Kraybill', 'Brian_Kraybill@hms.harvard.edu', 'bkraybill', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'David', 'Fletcher', 'david_fletcher@hms.harvard.edu', 'dfletcher', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'David', 'Wrobel', 'david_wrobel@hms.harvard.edu', 'dwrobel', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', 'dhw11');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Kyungae', 'Lee', 'kyungae_lee@hms.harvard.edu', 'klee', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'medicinalChemistUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Katrina', 'Schulberg', 'Katrina_Schulberg@hms.harvard.edu', 'kschulberg', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', 'kls4');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Melody', 'Tsui', 'Melody_Tsui@hms.harvard.edu', 'mtsui', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Su', 'Chiang', 'Su_Chiang@hms.harvard.edu', 'schiang', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Sean', 'Johnston', 'sean_johnston@hms.harvard.edu', 'sjohnston', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Stuart', 'Rudnicki', 'stuart_rudnicki@hms.harvard.edu', 'srudnicki', '981eb7e146cab5b17b4c7f5f12af441d36d0cc36', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Laura', 'Selfors', 'l.selfors@comcast.net', 'lselfors', '3257f52ce4d35c835fd7d6333c5afef67fb52243', 'ls67');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

commit;