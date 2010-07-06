BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4280,
current_timestamp,
'added cherry_pick_request.keep_source_plate_cherry_picks_together';

alter table cherry_pick_request add column keep_source_plate_cherry_picks_together bool not null default true;

COMMIT;
