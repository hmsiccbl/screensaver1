BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3567,
current_timestamp,
'removed checklist_item_event.version';

alter table checklist_item_event drop column version;

COMMIT;
