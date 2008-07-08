/* Creates a 'dev' (developer) and 'guest' (admin) account with passwords same as login ID. */

begin;

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Guest', '', 'guest', '35675e68f4b5af7b995d9205ad0fc43842f16450');
insert into screening_room_user (screensaver_user_id, user_classification, non_screening_user) values (currval('screensaver_user_id_seq'), 'ICCB-L/NSRB staff', false);
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screeningRoomUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'compoundScreeningRoomUser';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'rnaiScreeningRoomUser';

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Screensaver', 'Developer', '', 'dev', '34c6fceca75e456f25e7e99531e2425c6c1de443');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'developer';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'librariesAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'cherryPickAdmin';

commit;