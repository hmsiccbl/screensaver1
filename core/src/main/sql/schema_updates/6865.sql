BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6865,
current_timestamp,
'adjust expirable flag of RNAi user agreement checklist item';

/* TODO: ICCBL-specific */
update checklist_item set is_expirable=true where item_name = 'Current RNAi User Agreement active';

COMMIT;