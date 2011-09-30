BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6495,
current_timestamp,
'add fields date_loaded, and date_publicly_available to the audited entity classes (Activity, AttachedFile, ChecklistItemEvent, CherryPickRequest, Copy, Library,
Plate, Screen, ScreenResult, and ScreensaverUser)';

alter table activity add column date_loaded TIMESTAMP;
alter table activity add column date_publicly_available TIMESTAMP;

alter table attached_file add column date_loaded TIMESTAMP;
alter table attached_file add column date_publicly_available TIMESTAMP;

alter table checklist_item_event add column date_loaded TIMESTAMP;
alter table checklist_item_event add column date_publicly_available TIMESTAMP;

alter table cherry_pick_request add column date_loaded TIMESTAMP;
alter table cherry_pick_request add column date_publicly_available TIMESTAMP;

alter table copy add column date_loaded TIMESTAMP;
alter table copy add column date_publicly_available TIMESTAMP;

alter table library add column date_loaded TIMESTAMP;
alter table library add column date_publicly_available TIMESTAMP;

alter table plate add column date_loaded TIMESTAMP;
alter table plate add column date_publicly_available TIMESTAMP;

alter table screen add column date_loaded TIMESTAMP;
alter table screen add column date_publicly_available TIMESTAMP;

alter table screen_result add column date_loaded TIMESTAMP;
alter table screen_result add column date_publicly_available TIMESTAMP;

alter table screensaver_user add column date_loaded TIMESTAMP;
alter table screensaver_user add column date_publicly_available TIMESTAMP;

commit;