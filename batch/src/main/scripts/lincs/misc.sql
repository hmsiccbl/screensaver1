insert into attached_file_type (attached_file_type_id, value, for_entity_type) values (nextval('attached_file_type_id_seq'), 'Publication', 'screen');
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-NMR');
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-LCMS');
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-HPLC');
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'Study-File');

