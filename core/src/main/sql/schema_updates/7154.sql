BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7154,
current_timestamp,
'update cell table with audited abstract entity fields';

alter table cell add column date_created timestamp;
update cell set date_created = now();
alter table cell alter column date_created set not null;
alter table cell add column date_loaded timestamp;
alter table cell add column date_publicly_available timestamp;
alter table cell add column created_by_id int4;

create table cell_update_activity (
    cell_id int4 not null,
    update_activity_id int4 not null unique,
    primary key (cell_id, update_activity_id));
                
alter table cell 
  add constraint FK2E896266AB751E 
  foreign key (created_by_id) 
  references screensaver_user;
        
COMMIT;
