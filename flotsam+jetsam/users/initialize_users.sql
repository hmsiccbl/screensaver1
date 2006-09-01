delete from role_user_link;
delete from screensaver_user_role;
delete from screensaver_user;

insert into screensaver_user_role (screensaver_user_role_id, version, role_name) values (1, 1, 'user');
insert into screensaver_user_role (screensaver_user_role_id, version, role_name) values (2, 1, 'developer');

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (1, 1, 'today', '', 'Guest', '<guest>@hms.harvard.edu', 'guest', '35675e68f4b5af7b995d9205ad0fc43842f16450');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (1,1);

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (2, 1, 'today', 'Andrew', 'Tolopko', 'developer1@hms.harvard.edu', 'ant', 'd015cc465bdb4e51987df7fb870472d3fb9a3505');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (2,2);

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (3, 1, 'today', 'John', 'Sullivan', 'developer2@hms.harvard.edu', 's', '');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (2,3);

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, ecommons_id) values (4, 1, 'today', 'Andrew', 'Tolopko', 'andrew_tolopko@hms.harvard.edu', 'ant4');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (1,4);

insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, ecommons_id) values (5, 1, 'today', 'John', 'Sullivan', 'john_sullivan@hms.harvard.edu', 'js163');
insert into role_user_link (screensaver_user_role_id, screensaver_user_id) values (1,5);
