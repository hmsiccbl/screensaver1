BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5523,
current_timestamp,
'convert status_item from entity to element collection';

alter table status_item rename to screen_status_item;
alter table screen_status_item drop status_item_id;
alter table screen_status_item drop version;
alter table screen_status_item rename status_value to status;

alter table screen_status_item add primary key (screen_id, status, status_date);

drop sequence status_item_id_seq;

COMMIT;
