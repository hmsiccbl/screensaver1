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
  
alter table cell_update_activity 
    add constraint FKD53E98E87448DB73 
    foreign key (cell_id) 
    references cell;

alter table cell_update_activity 
    add constraint FKD53E98E88BCC1B97 
    foreign key (update_activity_id) 
    references administrative_activity;
  
COMMIT;
