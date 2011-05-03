BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4453,
current_timestamp,
'add unique index to assay_well';

create unique index assay_well_screen_result_id_key on assay_well (screen_result_id, well_id);

drop index if exists assay_well_unique_index;

COMMIT;
