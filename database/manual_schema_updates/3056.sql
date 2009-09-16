/* ICCB-L specific!  Modify as necessary for your facility's data */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3056,
current_timestamp,
'add "qpcr email list" checklist items; update "marcus user agreements" checklist items';

UPDATE checklist_item set order_statistic = order_statistic + 4 where checklist_item_group = 'Mailing Lists & Wikis' and order_statistic >= 4;
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 4, 't', 'Mailing Lists & Wikis', 'Added to QPCR email list'
);
UPDATE checklist_item set order_statistic = order_statistic - 3 where checklist_item_group = 'Mailing Lists & Wikis' and order_statistic >= 8;

UPDATE checklist_item set item_name = 'Marcus - ICCBL user agreement signed' where item_name = 'Marcus - ICCBL user agreement (Harvard Screeners) signed';
DELETE FROM checklist_item where item_name = 'Marcus - ICCBL user agreement (Non-Harvard Screeners) signed';

COMMIT;