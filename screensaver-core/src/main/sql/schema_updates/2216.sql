BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2216,
current_timestamp,
'annotation_value unique key on (annotation_type_id, reagent_id)';

ALTER TABLE annotation_value ADD CONSTRAINT annotation_value_annotation_type_id_key UNIQUE (annotation_type_id, reagent_id);

COMMIT;