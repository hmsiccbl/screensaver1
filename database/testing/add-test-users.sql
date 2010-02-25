begin;

/* Create a user account w/o login privileges */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.no.login', 'User', 'NoLogin', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');

/* Create a user account w/only login privileges */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.login', 'User', 'Login', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';

/* Create a RNAi+Login user account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.rnai', 'User', 'RNAi', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'rnaiScreens';

/* Create a Small Molecule 1+Login user account w/o screen result data. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.sm1.nodata', 'User', 'SM1NoData', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl3SharedScreens';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl2MutualPositives';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl1MutualScreens';

/* Create a Small Molecule 1+Login user account with screen result data. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.sm1.data', 'User', 'SM1Data', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl3SharedScreens';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl2MutualPositives';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl1MutualScreens';
/* TODO: create screen result */

/* Create a Small Molecule 2+Login user account with screen result data. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.sm2', 'User', 'SM2', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl3SharedScreens';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl2MutualPositives';
/* TODO: create screen result */

/* Create a Small Molecule 3+Login user account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.sm3', 'User', 'SM3', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl3SharedScreens';

/* Create a Small Molecule 3+RNAi+Login user account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'user.sm3.rnai', 'User', 'SM3RNAi', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'rnaiScreens';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'smDsl3SharedScreens';

/* Create a readEverything admin account, with no other admin privileges. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.read', 'Admin', 'ReadEverything', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';

/* Create a Marcus admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.marcus', 'Admin', 'Marcus', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'marcusAdmin';

/* Create a Gray admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.gray', 'Admin', 'Gray', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'grayAdmin';

/* Create a ChecklistItems admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.ci', 'Admin', 'ChecklistItems', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userChecklistItemsAdmin';

/* Create a Users admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.users', 'Admin', 'Users', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userChecklistItemsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';

/* Create a Users+LabHeads admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.labheads', 'Admin', 'LabHeads', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userChecklistItemsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'labHeadsAdmin';

/* Create a Users+UserRoles admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.userroles', 'Admin', 'UserRoles', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userChecklistItemsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userRolesAdmin';

/* Create a Screens admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.screens', 'Admin', 'Screens', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';

/* Create a Screens+Billing admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.billing', 'Admin', 'Billing', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';

/* Create a ScreenResults admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.screenresults', 'Admin', 'ScreenResults', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';

/* Create a CPR admin account admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.cprs', 'Admin', 'CPR', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickRequestsAdmin';

/* Create a Libraries admin account. */
insert into screensaver_user (screensaver_user_id, version, date_created, login_id, first_name, last_name, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'admin.libs', 'Admin', 'Libraries', '0ec11041bc88659af8e077fb2e696b8e1ddcc687');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';


/*commit;*/