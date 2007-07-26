BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1619, 
current_timestamp, 
'added compound.is_pubchem_cid_list_upgrader_{failed,successful}';
 
ALTER TABLE compound
  ADD COLUMN is_pubchem_cid_list_upgrader_failed BOOL DEFAULT FALSE,
  ADD COLUMN is_pubchem_cid_list_upgrader_successful BOOL DEFAULT FALSE;

ALTER TABLE compound
  ALTER COLUMN is_pubchem_cid_list_upgrader_failed set not null,
  ALTER COLUMN is_pubchem_cid_list_upgrader_successful set not null;

COMMIT;