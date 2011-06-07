BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5965,
current_timestamp,
'split "Letter of Support" attached file type into subtypes';

/* TODO: ICCBL-specific update */
update attached_file_type set value = 'Letter of Support - Other' where value = 'Letter of Support';
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_id_seq'), 'user', 'Letter of Support - RNAi');
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_id_seq'), 'user', 'Letter of Support - Small Molecule');
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_id_seq'), 'user', 'Letter of Support - RNAi & Small Molecule');

COMMIT;
