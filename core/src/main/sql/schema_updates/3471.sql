BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3471,
current_timestamp,
'added columns time_point_ordinal and z_ordinal';

alter table result_value_type add column time_point_ordinal integer;
alter table result_value_type add column zdepth_ordinal integer;

COMMIT;