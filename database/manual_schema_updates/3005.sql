BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3005,
current_timestamp,
'add marcus user checklist item';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 5, 'f', 'Forms', 'Marcus Library MTA Signed'
);

COMMIT;