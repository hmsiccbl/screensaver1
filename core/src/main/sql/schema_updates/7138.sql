BEGIN;


--INSERT INTO schema_history (screensaver_revision, date_updated, comment)
--SELECT
--7138,
--current_timestamp,
--'add "Publication", "Other" attached_file_types';

alter table attached_file_type drop constraint attached_file_type_value_key;
alter table attached_file_type add constraint attached_file_type_key unique ( value, for_entity_type );

/* TODO: ICCBL-specific update */
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_type_id_seq'), 'user', 'Publication');
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_type_id_seq'), 'user', 'Other');

COMMIT;
