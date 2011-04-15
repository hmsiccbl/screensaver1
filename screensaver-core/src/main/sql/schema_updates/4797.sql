BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4797,
current_timestamp,
'study screens are identified by project phase';

update screen set project_phase = 'Annotation', facility_id = substr(facility_id, 2) where facility_id like 'S%';

COMMIT;
