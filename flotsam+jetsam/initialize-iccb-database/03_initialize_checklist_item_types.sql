BEGIN;
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  1, 0, 1, 't', 'ID submitted for access to screening room'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  2, 0, 2, 't', 'Added to ICCB-L/NSRB screening users list'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  3, 0, 3, 't', 'ICCB-L/NSRB server account set up (general account)'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  4, 0, 4, 't', 'ID submitted for C-607 access'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  5, 0, 5, 't', 'Added to autoscope users list'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  6, 0, 6, 't', 'ICCB-L/NSRB image file server access set up'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  7, 0, 7, 't', 'Added to PI email list'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  8, 0, 8, 't', 'Historical - ICCB server account requested'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  9, 0, 9, 'f', 'Data sharing agreement signed'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  10, 0, 10, 'f', 'Non-HMS Biosafety Training Form on File'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  11, 0, 11, 'f', 'CellWoRx training'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  12, 0, 12, 'f', 'Autoscope training'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  13, 0, 13, 'f', 'Image Analysis I training'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  14, 0, 14, 'f', 'Image Xpress Micro Training'
);
INSERT INTO checklist_item (
  checklist_item_id, version, order_statistic, is_expirable, item_name
) VALUES (
  15, 0, 15, 'f', 'Evotech Opera Training'
);
COMMIT;