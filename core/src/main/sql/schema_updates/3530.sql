BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3530,
current_timestamp,
'removed attached file unique constraint';

drop index if exists attached_file_unique;

COMMIT;