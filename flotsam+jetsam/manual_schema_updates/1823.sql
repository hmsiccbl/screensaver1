BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1823,
current_timestamp,
'add vendor name to annotation value';

ALTER TABLE annotation_value ADD vendor_name varchar(255);
ALTER TABLE annotation_value RENAME vendor_identifier TO reagent_identifier;
ALTER TABLE annotation_value ALTER reagent_identifier TYPE varchar(255);
UPDATE annotation_value SET vendor_name = 'Dharmacon';
ALTER TABLE annotation_value ALTER vendor_name SET NOT NULL;

COMMIT;