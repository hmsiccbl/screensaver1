/* This SQL script initializes the users in the Screensaver database.         */
/*                                                                            */
/* Two types of users are supported: administrators and screening room        */
/* users.  All users will have an record in the screensaver_user              */
/* table.  Administrators must have a related record in the                   */
/* administrator_user table.  Screening room users must have a related        */
/* record in the screensaver_user table.                                      */
/*                                                                            */
/* The 'login_id' field will be used to authenticate the admin/user, along    */
/* with the 'digested_password' field that contains a SHA1-hashed             */
/* value of the user's password.  For ICCB-L web deployments only, the        */
/* optional 'ecommons_id' can also be used for login (and which has no        */
/* accompanying password in the database).                                    */
/*                                                                            */
/* Need to generate a SHA1 hashed password?  Try:                             */
/*   perl -e 'use Digest::SHA1; my $sha1 = Digest::SHA1->new; \               */
/*            $sha1->add("YOUR_PASSWORD"); print $sha1->hexdigest(), \        */
/*            "\n";'                                                          */
/*                                                                            */
/* The screensaver_user_role_type table is used to specify the roles          */
/* associated with each user, which control the user's authorizations.        */
/* The list of valid user roles can be found in the                           */
/* edu.harvard.med.screensaver.model.users.ScreensaverUserRole Java           */
/* class.  Note that administrator users must only be given                   */
/* administrator roles, and screening room users must only be given           */
/* user roles.                                                                */


begin;

/* guest has empty password */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Guest', 'guest@hms.harvard.edu', 'guest', 'da39a3ee5e6b4b0d3255bfef95601890afd80709');
insert into screening_room_user (screensaver_user_id, user_classification, non_screening_user) values (currval('screensaver_user_id_seq'), 'ICCB-L/NSRB staff', false);
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screeningRoomUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Andrew', 'Tolopko', 'andrew_tolopko@hms.harvard.edu', 'ant', 'd015cc465bdb4e51987df7fb870472d3fb9a3505', 'ant4');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'John', 'Sullivan', 'john_sullivan@hms.harvard.edu', 's', '102efc1a35e83cc91266bc8c6d34ea50c8eef0bc', 'js163');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Caroline', 'Shamu', 'caroline_shamu@hms.harvard.edu', 'cshamu', '2c762bfe28e265e516a570341cce61a761d6fe1e', 'ces6');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Ruchir', 'Shaw', 'ruchir_shaw@hms.harvard.edu', 'rshah', '2c8183d975d6b27271a2b16cd044d663fa09c523', 'rcs12');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'medicinalChemistUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Tiao', 'Xie', 'Tiao_Xie@hms.harvard.edu', 'tiaoxie', 'usetheecommons', 'tx4');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'David', 'Fletcher', 'david_fletcher@hms.harvard.edu', 'dfletcher', '5a86c7155e31e6236ae7006e2afc78baa002f211', 'df30');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'David', 'Wrobel', 'david_wrobel@hms.harvard.edu', 'dwrobel', '491162cda9054e8b010fef7b19996fda6c2aaa3d', 'djw11');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Kyungae', 'Lee', 'kyungae_lee@hms.harvard.edu', 'klee', '03e1c66f01302049d9c831d71e75ffe94a8a7b78', 'kl66');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'medicinalChemistUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Katrina', 'Schulberg', 'Katrina_Schulberg@hms.harvard.edu', 'kschulberg', '3cedbd68421edd8d3eca5e950c9f8211d9aa4cff', 'kls4');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Melody', 'Tsui', 'Melody_Tsui@hms.harvard.edu', 'mtsui', '5a3414e8095a5a241b185ab7b5098f7cbd448768', 'ymt3');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Su', 'Chiang', 'Su_Chiang@hms.harvard.edu', 'schiang', '92317c686edbd519de9a6db6abb9ea2acf230576', 'slc9');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Sean', 'Johnston', 'sean_johnston@hms.harvard.edu', 'sjohnston', 'c06a65c1c72280dee1fad18017975e95c9da88b4', 'smj9');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Stewart', 'Rudnicki', 'stewart_rudnicki@hms.harvard.edu', 'srudnicki', '2f78d8559c6e31a10fcb3e3b9eb70ef55bef02eb', 'sr50');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Laura', 'Selfors', 'l.selfors@comcast.net', 'lselfors', '3257f52ce4d35c835fd7d6333c5afef67fb52243', 'ls67');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Nathan', 'Moerke', 'nathan_moerke@hms.harvard.edu', 'nmoerke', 'd805a86e89cadb3f27357f75257ae737c7b800cc', null);
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';


commit;