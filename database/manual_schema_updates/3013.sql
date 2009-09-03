BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3013,
current_timestamp,
'add marcus user agreement signed checklist items';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 6, 'f', 'Forms', 'Marcus - ICCBL user agreement (Harvard Screeners) signed'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 7, 'f', 'Forms', 'Marcus - ICCBL user agreement (Non-Harvard Screeners) signed'
);

COMMIT;