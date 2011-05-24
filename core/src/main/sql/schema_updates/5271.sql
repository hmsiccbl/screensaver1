BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5271,
current_timestamp,
'add reagent-to-attached file relationship';

alter table attached_file add reagent_id int4;

alter table attached_file 
    add constraint fk_attached_file_to_reagent 
    foreign key (reagent_id) 
    references reagent;

/* TODO: add attached file types for reagents that are appropriate for your facility */
/*
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-NMR'); 
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-LCMS'); 
insert into attached_file_type (for_entity_type, attached_file_type_id, value) values ('reagent', nextval('attached_file_type_id_seq'), 'QC-HPLC'); 
*/

COMMIT;
