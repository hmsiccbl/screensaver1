BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2599,
current_timestamp,
'added ChecklistItem.group';

TRUNCATE checklist_item_event;
DROP TABLE checklist_item CASCADE;

create table checklist_item (
    checklist_item_id int4 not null,
    checklist_item_group text not null,
    is_expirable bool not null,
    item_name text not null unique,
    order_statistic int4 not null,
    version int4 not null,
    primary key (checklist_item_id),
    unique (checklist_item_group, order_statistic)
);

COMMIT;
