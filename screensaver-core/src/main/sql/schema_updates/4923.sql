BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4923,
current_timestamp,
'make plate an AuditedAbstractEntity';

alter table plate add column date_created timestamp;
update plate set date_created = (select date_created from copy c where c.copy_id = plate.copy_id);
alter table plate alter column date_created set not null;
alter table plate add column created_by_id int4;
update plate set created_by_id = (select created_by_id from copy c where c.copy_id = plate.copy_id);

alter table plate 
    add constraint FK65CDB1666AB751E 
    foreign key (created_by_id) 
    references screensaver_user;

create table plate_update_activity (
    plate_id int4 not null,
    update_activity_id int4 not null unique,
    primary key (plate_id, update_activity_id)
);

COMMIT;
