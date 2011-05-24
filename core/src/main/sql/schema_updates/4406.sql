BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4406,
current_timestamp,
'add cell_line column to data_column, add concentration field to well, add mulitivalue chembl_id table linked to the small_molecule_reagent';

alter table data_column add column cell_line text;
alter table well add column concentration numeric(15, 12);

create table small_molecule_chembl_id (
  reagent_id int4 not null,
  chembl_id int4 not null,
  primary key (reagent_id, chembl_id)
);

alter table small_molecule_chembl_id 
    add constraint fk_small_molecule_chembl_id_to_small_molecule_reagent 
    foreign key (reagent_id) 
    references small_molecule_reagent;


COMMIT;
