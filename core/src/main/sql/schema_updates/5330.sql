BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5330,
current_timestamp,
'add Screen pubchem bioassay fields';

alter table screen add column pubchem_assay_id int4;
alter table screen add column pubchem_deposited_date date;

COMMIT;
