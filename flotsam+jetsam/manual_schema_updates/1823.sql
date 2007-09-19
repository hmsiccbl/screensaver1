BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1823,
current_timestamp,
'add vendor name to annotation value';

ALTER TABLE annotation_value ADD vendor_name varchar(255);
ALTER TABLE annotation_value ADD reagent_identifier varchar(255);
UPDATE annotation_value SET reagent_identifier = vendor_identifier;
ALTER TABLE annotation_value DROP vendor_identifier;
UPDATE annotation_value SET vendor_name = 'Dharmacon';
ALTER TABLE annotation_value ALTER vendor_name SET NOT NULL;

COMMIT;