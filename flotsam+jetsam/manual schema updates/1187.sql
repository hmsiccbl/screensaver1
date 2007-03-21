insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password, ecommons_id) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Nate', 'Moerke', 'nate_moerke@hms.harvard.edu', null, null, 'njm2');
insert into administrator_user (screensaver_user_id) values (currval('screensaver_user_id_seq'));
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'readEverythingAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screensAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screenResultsAdmin';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'usersAdmin';
