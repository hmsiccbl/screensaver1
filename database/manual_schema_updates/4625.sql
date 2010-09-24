BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4625,
current_timestamp,
'make copy an AuditedAbstractEntity';

alter table copy add column date_created timestamp;
update copy set date_created = (select date_created from library l where l.library_id = copy.library_id);
alter table copy alter column date_created set not null;
alter table copy add column created_by_id int4;
alter table copy
    add constraint FK2EAF7566AB751E 
    foreign key (created_by_id) 
    references screensaver_user;

create table copy_update_activity (
    copy_id int4 not null,
    update_activity_id int4 not null unique,
    primary key (copy_id, update_activity_id));

COMMIT;
