/* 
 * initialize the attached file types
 */

begin;

insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Application', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Primary Screen Report', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Letter of Support', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Screener Correspondence', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'MIARE Document', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Publication', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Data QC Report', 'screen');


commit;