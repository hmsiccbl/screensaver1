/* ICCB-L specific!  Modify as necessary for your facility's data */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3044,
current_timestamp,
'add user agreement signed checklist items';

UPDATE checklist_item set order_statistic = order_statistic + 9 where checklist_item_group = 'Forms' and order_statistic >= 2;

UPDATE checklist_item set item_name = '2009 Small Molecule User Level ONE (1) agreement signed', is_expirable = true where checklist_item_group = 'Forms' and item_name = '2009 Small Molecule User agreement signed';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 2, 't', 'Forms', '2009 Small Molecule User Level TWO (2) agreement signed'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 3, 't', 'Forms', '2009 Small Molecule User Level THREE (3) agreement signed'
);

UPDATE checklist_item set order_statistic = order_statistic - 7 where checklist_item_group = 'Forms' and order_statistic >= 11;

COMMIT;