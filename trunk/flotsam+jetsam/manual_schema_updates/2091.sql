BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2091,
current_timestamp,
'remove compound.is_pubchem_cid_list_upgrader_{successful,failed}';

alter table compound drop column is_pubchem_cid_list_upgrader_successful;
alter table compound drop column is_pubchem_cid_list_upgrader_failed;

COMMIT;