BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3805,
current_timestamp,
'add screen.min_allowed_data_privacy_expiration_date and screen.max_allowed_data_privacy_expiration_date, and screen.data_privacy_expiration_notified_date, screening_room_user.last_notified_smua_checklist_item_event_id; set min/max_allowed_data_privacy_expiration_date = data_privacy_expiration_date';

alter table screen add max_allowed_data_privacy_expiration_date date;
alter table screen add min_allowed_data_privacy_expiration_date date;
alter table screen add data_privacy_expiration_notified_date date;

/* initialize these values to the current setting; initial data migration decision to use the current values as the lower bound -sde4 */
update screen set min_allowed_data_privacy_expiration_date = data_privacy_expiration_date;

alter table screening_room_user add last_notified_smua_checklist_item_event_id int4;

COMMIT;
