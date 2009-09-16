BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3038,
current_timestamp,
'add user agreement signed checklist items';

UPDATE checklist_item set order_statistic = order_statistic + 7 where checklist_item_group = 'Forms' and order_statistic >= 2;

UPDATE checklist_item set item_name = 'Old data sharing agreement signed' where checklist_item_group = 'Forms' and item_name = 'Data sharing agreement signed';
UPDATE checklist_item set item_name = '2009 Small Molecule User agreement signed' where checklist_item_group = 'Forms' and item_name = '2009 Data sharing agreement signed';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 2, 'f', 'Forms', '2009 RNAi User agreement signed'
);

UPDATE checklist_item set order_statistic = order_statistic - 6 where checklist_item_group = 'Forms' and order_statistic >= 9;

COMMIT;