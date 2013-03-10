/* ICCB-L specific!  Modify as necessary for your facility's data */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7312,
current_timestamp,
'add "Added to mouse imager email list" "IVIS wiki access" checklist items';

INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 8, 't', 'Mailing Lists & Wikis', 'Added to mouse imager email list'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, checklist_item_group, item_name
) VALUES (
  nextval('checklist_item_id_seq'), 0, 9, 't', 'Mailing Lists & Wikis', 'IVIS wiki access'
);

COMMIT;