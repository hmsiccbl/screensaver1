BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1336;

/* I think this temporary index does help speed the following queries; it does get used, according to EXPLAIN... */
CREATE INDEX activity_indicator_type_index ON result_value_type (activity_indicator_type);

UPDATE result_value_type_result_values set value = '1' from result_value_type rvt where rvt.result_value_type_id = result_value_type_result_values.result_value_type_id and rvt.activity_indicator_type = 'Partition' and value = 'W';
UPDATE result_value_type_result_values set value = '2' from result_value_type rvt where rvt.result_value_type_id = result_value_type_result_values.result_value_type_id and rvt.activity_indicator_type = 'Partition' and value = 'M';
UPDATE result_value_type_result_values set value = '3' from result_value_type rvt where rvt.result_value_type_id = result_value_type_result_values.result_value_type_id and rvt.activity_indicator_type = 'Partition' and value = 'S';
UPDATE result_value_type_result_values set value = '0' from result_value_type rvt where rvt.result_value_type_id = result_value_type_result_values.result_value_type_id and rvt.activity_indicator_type = 'Partition' and value = '';

DROP INDEX activity_indicator_type_index;

COMMIT;
