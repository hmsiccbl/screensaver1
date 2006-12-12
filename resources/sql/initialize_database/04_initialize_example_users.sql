/* password=scr33n3r */
insert into screensaver_user (screensaver_user_id, version, date_created, first_name, last_name, email, login_id, digested_password) values (nextval('screensaver_user_id_seq'), 1, 'today', 'Compound', 'Screener', 'cscreener@hms.harvard.edu', 'cscreener', '4282a78386b751f631f6caa61a05ca6b0d3eda75'); 
insert into screening_room_user (screensaver_user_id, user_classification, non_screening_user) values (currval('screensaver_user_id_seq'), 'ICCB-L/NSRB staff', false);
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'screeningRoomUser';
insert into screensaver_user_role_type (screensaver_user_id, screensaver_user_role) select currval('screensaver_user_id_seq'), 'compoundScreeningRoomUser';

insert into screen (screen_id, version, date_created, screen_type, title, summary, lead_screener_id, lab_head_id, screen_number) select 999, 0, now(), 'Small Molecule', 'Test screen', 'For use with Compound Screener account', currval('screensaver_user_id_seq'), currval('screensaver_user_id_seq'), 999;
