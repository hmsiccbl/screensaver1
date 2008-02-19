BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2148,
current_timestamp,
'update rvt.name to be unique within a screen result';

UPDATE result_value_type SET name = result_value_type.name || ' [' || result_value_type.ordinal || ']' 
FROM result_value_type rvt 
WHERE rvt.result_value_type_id <> result_value_type.result_value_type_id 
AND rvt.screen_result_id = result_value_type.screen_result_id
AND rvt.name = result_value_type.name;

COMMIT;