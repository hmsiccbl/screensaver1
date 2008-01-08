BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2092,
current_timestamp,
'replace single compound.chembank_id with set of chembank ids';

alter table compound drop column chembank_id;

create table compound_chembank_id (
  compound_id text not null,
  chembank_id text not null,
  primary key (compound_id, chembank_id)
);

alter table compound_chembank_id 
  add constraint fk_compound_chembank_id_to_compound 
  foreign key (compound_id) 
  references compound;

COMMIT;