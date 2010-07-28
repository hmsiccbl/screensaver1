BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4062,
current_timestamp,
'associate screen result with data loading activity';

insert into screen_result_update_activity (screen_result_id, update_activity_id)
select distinct sr.screen_result_id, ap.screen_result_data_loading_id
from assay_plate ap join screen s using(screen_id) join screen_result sr using(screen_id)
where ap.screen_result_data_loading_id is not null;


COMMIT;
