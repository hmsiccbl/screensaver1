BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2026,
current_timestamp,
'fixed well_molfile ordinal';

UPDATE well_molfile SET ordinal = 0;

COMMIT;