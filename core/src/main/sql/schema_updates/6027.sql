BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6027,
current_timestamp,
'set all silencing_reagent sequences to restricted';

/** TODO: ICCB-L only, as this update moves the IccbEntityViewPolicy logic for sequence viewing restriction from source code into database field; all silencing reagent sequences were considered admin-only previously */  
update silencing_reagent set is_restricted_sequence = true;

COMMIT;
