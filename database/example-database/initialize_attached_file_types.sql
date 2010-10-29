/* 
 * Initialize the attached file types.  These file types are simply categories that can be assigned 
 * to attached (uploaded) files that are associated with a screen or a user account in Screensaver.
 */

begin;

/* The 'Publication' attached file type is required */
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Publication', 'screen');

/* Example attached file types for screens. */
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Application', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Primary Screen Report', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Letter of Support', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Screener Correspondence', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'MIARE Document', 'screen');
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Data QC Report', 'screen');

/* Example attached file types for users. */
insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'User Agreement', 'user');

commit;