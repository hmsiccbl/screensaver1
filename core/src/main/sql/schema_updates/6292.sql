BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6292,
current_timestamp,
'update to adjust the attached_file_type_id_seq which was set using the attached_file_id_seq in 5965.sql';

create table attached_file_type_fix as
select * from attached_file_type where value in ('Letter of Support - RNAi','Letter of Support - Small Molecule','Letter of Support - RNAi & Small Molecule');
alter table attached_file_type drop constraint attached_file_type_value_key;

insert into attached_file_type (attached_file_type_id, for_entity_type, value) select nextval('attached_file_type_id_seq'), for_entity_type, value from attached_file_type_fix where value = 'Letter of Support - RNAi';
update attached_file set attached_file_type_id = currval('attached_file_type_id_seq') where attached_file_type_id = (select attached_file_type_id from attached_file_type_fix where value = 'Letter of Support - RNAi');


insert into attached_file_type (attached_file_type_id, for_entity_type, value) select nextval('attached_file_type_id_seq'), for_entity_type, value from attached_file_type_fix where value = 'Letter of Support - Small Molecule';
update attached_file set attached_file_type_id = currval('attached_file_type_id_seq') where attached_file_type_id = (select attached_file_type_id from attached_file_type_fix where value = 'Letter of Support - Small Molecule');


insert into attached_file_type (attached_file_type_id, for_entity_type, value) select nextval('attached_file_type_id_seq'), for_entity_type, value from attached_file_type_fix where value = 'Letter of Support - RNAi & Small Molecule';
update attached_file set attached_file_type_id = currval('attached_file_type_id_seq') where attached_file_type_id = (select attached_file_type_id from attached_file_type_fix where value = 'Letter of Support - RNAi & Small Molecule');

delete from attached_file_type where attached_file_type_id in (select attached_file_type_id from attached_file_type_fix);
drop table attached_file_type_fix;

alter table attached_file_type add constraint attached_file_type_value_key unique (value);

commit;
