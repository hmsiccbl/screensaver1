BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4089,
current_timestamp,
'renamed library_screening.is_for_screener_provided_plates to is_for_external_library_plates';

alter table library_screening rename column is_for_screener_provided_plates to is_for_external_library_plates;

COMMIT;
