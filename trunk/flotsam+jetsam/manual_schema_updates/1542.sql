BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1542, 
current_timestamp, 
'renamed result_value_type{,result_values} fields: s/hit/positive/, s/(activity_)?indicator/positive_indicator/';
 
ALTER TABLE result_value_type RENAME COLUMN activity_indicator TO positive_indicator;
ALTER TABLE result_value_type RENAME COLUMN activity_indicator_type TO positive_indicator_type;
ALTER TABLE result_value_type RENAME COLUMN indicator_cutoff TO positive_indicator_cutoff;
ALTER TABLE result_value_type RENAME COLUMN indicator_direction TO positive_indicator_direction;
ALTER TABLE result_value_type RENAME COLUMN hits TO positives_count;
ALTER TABLE result_value_type_result_values RENAME COLUMN hit TO positive;

COMMIT;