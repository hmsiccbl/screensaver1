BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4598,
current_timestamp,
'change copy to generated ID';

create sequence copy_id_seq;

alter table plate drop constraint fk_plate_to_copy;
alter table plate drop constraint plate_key;
alter table well_volume_adjustment drop constraint fk_well_volume_adjustment_to_copy;
alter table copy drop constraint copy_pkey;

alter table copy rename column copy_id to _copy_id;
alter table plate rename column copy_id to _copy_id;
alter table well_volume_adjustment rename column copy_id to _copy_id;

alter table copy add column copy_id int4;
alter table plate add column copy_id int4;
alter table well_volume_adjustment add column copy_id int4;

update copy set copy_id = nextval('copy_id_seq');
alter table copy add primary key (copy_id);

update plate set copy_id = (select copy_id from copy c where c._copy_id = plate._copy_id);
alter table plate alter column copy_id set not null;

update well_volume_adjustment set copy_id = (select copy_id from copy c where c._copy_id = well_volume_adjustment._copy_id);
alter table well_volume_adjustment alter column copy_id set not null;

alter table plate drop column _copy_id;
alter table well_volume_adjustment drop column _copy_id;
alter table copy drop column _copy_id;

alter table plate add constraint plate_key unique (copy_id, plate_number);

alter table plate 
    add constraint fk_plate_to_copy 
    foreign key (copy_id) 
    references copy;

alter table well_volume_adjustment 
    add constraint fk_well_volume_adjustment_to_copy 
    foreign key (copy_id) 
    references copy;
  
COMMIT;
