/* 
 * Creates a 'guest' account and a 'dev' (developer) account, with
 * passwords same as login ID. You may copy these two examples to
 * create additional users, as desired.  Note that the 'dev'
 * administrator account includes the special 'developer' role, which
 * is generally not needed when creating normal administrator
 * accounts.  The "digested" password value is a SHA1 cryptographic
 * hash value of the plaintext password (there are many online SHA
 * calculators you can use to generate these values.)
 */

begin;

/* Create a 'guest' user account. */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Guest', '', 'guest', '35675e68f4b5af7b995d9205ad0fc43842f16450');
insert into screening_room_user (screensaver_user_id, user_classification) values (currval('screensaver_user_id_seq'), 'Other');
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screeningRoomUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'compoundScreeningRoomUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'rnaiScreeningRoomUser';

/* Create a 'dev' administrator account. */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Developer', '', 'dev', '34c6fceca75e456f25e7e99531e2425c6c1de443');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensaverUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'userChecklistItemsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'labHeadsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'billingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'dataSharingLevelAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickRequestsAdmin';


commit;