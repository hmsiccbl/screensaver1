BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4449,
current_timestamp,
'add column ordinal to small_molecule_compound_name to support the "primary compound name" (GO specific feature)';

alter table small_molecule_compound_name add column ordinal int4;

/* populate the new ordinal column */
update small_molecule_compound_name set ordinal = (select count(*)-1 from small_molecule_compound_name smc2
  where smc2.reagent_id = small_molecule_compound_name.reagent_id
  and smc2.compound_name <= small_molecule_compound_name.compound_name order by small_molecule_compound_name.compound_name);
  
alter table small_molecule_compound_name alter column ordinal set not null;  
alter table small_molecule_compound_name drop constraint small_molecule_compound_name_pkey;
alter table small_molecule_compound_name add constraint small_molecule_compound_name_pkey primary key (reagent_id,ordinal);

COMMIT;
