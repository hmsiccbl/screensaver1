BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2925,
current_timestamp,
'add "2009 Data sharing agreement"';

UPDATE checklist_item set order_statistic = order_statistic + 4 where checklist_item_group = 'Forms';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 1, 'f', 'Forms', '2009 Data sharing agreement signed'
);

UPDATE checklist_item set order_statistic = order_statistic - 3 where checklist_item_group = 'Forms' and order_statistic > 1;

COMMIT