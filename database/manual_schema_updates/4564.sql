BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4564,
current_timestamp,
'change copy.usage_type values';

update copy set usage_type = 'Library Screening Plates' where usage_type = 'For Library Screening';
update copy set usage_type = 'Cherry Pick Stock Plates' where usage_type = 'For Cherry Pick Screening';

COMMIT;
