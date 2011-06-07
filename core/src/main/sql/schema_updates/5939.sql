BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5939,
current_timestamp,
'move "Letter of Support" attached file type from screen to user';

/* TODO: ICCBL-specific update */
update attached_file_type set for_entity_type = 'user' where value = 'Letter of Support';
update attached_file set screensaver_user_id = (select lead_screener_id from screen s where s.screen_id = attached_file.screen_id), 
screen_id = null 
where screen_id is not null 
and attached_file_type_id = (select attached_file_type_id from attached_file_type where value = 'Letter of Support');

COMMIT;
