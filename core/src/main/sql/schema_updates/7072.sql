BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7072,
current_timestamp,
'add max skipped wells per plate to cherry pick requests';


alter table cherry_pick_request add column max_skipped_wells_per_plate int4;

COMMIT;
