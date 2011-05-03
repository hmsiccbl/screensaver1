BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3255,
current_timestamp,
'added screen.pin_transfer_admin_activity_id';

alter table screen add column pin_transfer_admin_activity_id int4;

alter table screen 
    add constraint fk_screen_to_pin_transfer_admin_activity 
    foreign key (pin_transfer_admin_activity_id) 
    references administrative_activity;


COMMIT;