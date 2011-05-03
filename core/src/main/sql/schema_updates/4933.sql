BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4933,
current_timestamp,
'add library.solvent';

alter table library add column solvent text;
update library set solvent = 'RNAi buffer' where library.screen_type = 'RNAi';
update library set solvent = 'DMSO' where library.screen_type = 'Small Molecule';
alter table library alter column solvent set not null;
  
COMMIT;
