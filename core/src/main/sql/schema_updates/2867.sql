BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2867,
current_timestamp,
'add is_not_applicable to checklist_item_event';

ALTER TABLE checklist_item_event ADD COLUMN is_not_applicable BOOL; 
UPDATE checklist_item_event SET is_not_applicable = false;
ALTER TABLE checklist_item_event ALTER COLUMN is_not_applicable SET NOT NULL;

COMMIT;