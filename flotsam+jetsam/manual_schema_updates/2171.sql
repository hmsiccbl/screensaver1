BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2171,
current_timestamp,
'cluster annotation_value on reagent_id';

CLUSTER annotation_value_reagent_id_index ON annotation_value;

COMMIT;