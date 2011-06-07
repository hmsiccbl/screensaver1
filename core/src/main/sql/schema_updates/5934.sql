BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5934,
current_timestamp,
'add assay_well.confirmed_positive_value';

alter table assay_well add column confirmed_positive_value text;
update assay_well set confirmed_positive_value = 
(select rv.value from result_value rv join data_column dc using(data_column_id) 
 where assay_well.well_id = rv.well_id 
 and dc.data_type = 'Confirmed Positive Indicator'
 and dc.screen_result_id = assay_well.screen_result_id)
from data_column dc where dc.screen_result_id = assay_well.screen_result_id and dc.data_type = 'Confirmed Positive Indicator';
create index assay_well_confirmed_positives_data_only_index on assay_well (confirmed_positive_value) where confirmed_positive_value is not null;

COMMIT;
