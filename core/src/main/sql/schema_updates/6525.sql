BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6525,
current_timestamp,
'remove redundant index screener_cherry_pick.screener_cherry_pick_key';

/* 1929.sql and 1955.sql added redundant constraint/index, so we'll drop one */
alter table screener_cherry_pick drop constraint screener_cherry_pick_key;

COMMIT;