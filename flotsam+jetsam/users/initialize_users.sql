delete from role_user_link;
delete from screensaver_user_role;
delete from screensaver_user;
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id) values (1, 1, 'today', 'first', 'last', 'first_last@hms.harvard.edu', 'test');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name) values (1, 1, 'user');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (1,1);
