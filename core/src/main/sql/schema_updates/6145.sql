/*
 * NOTE: this data migration script should not be necessary, since these fields are unused, at this time, outside of LINCS
 */
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6145,
current_timestamp,
'removed library.quality_control_information';

alter table library drop quality_control_information;

COMMIT;
