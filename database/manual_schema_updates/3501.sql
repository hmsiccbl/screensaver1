BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3501,
current_timestamp,
'converted attached file type to entity';

create table attached_file_type (
    for_entity_type varchar(31) not null,
    attached_file_type_id int4 not null,
    value text not null unique,
    primary key (attached_file_type_id)
);

create sequence attached_file_type_id_seq;

insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Application', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Primary Screen Report', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Letter of Support', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Screener Correspondence', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'MIARE Document', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Publication', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Data QC Report', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Marcus Application', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'NERCE screener supplies list', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'ICCB-L/NSRB Small Molecule User Agreement', 'user');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'ICCB-L/NSRB RNAi User Agreement', 'user');

alter table attached_file add column attached_file_type_id int4;
update attached_file set attached_file_type_id = (select attached_file_type_id from attached_file_type where value = file_type);
alter table attached_file alter column attached_file_type_id set not null;
alter table attached_file drop column file_type;

update attached_file_type set value = 'ICCB-L/NSRB Application' where value = 'Application';

alter table attached_file 
    add constraint fk_attached_file_to_attached_file_type 
    foreign key (attached_file_type_id) 
    references attached_file_type;

COMMIT;