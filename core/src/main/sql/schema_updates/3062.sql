BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3062,
current_timestamp,
'added CPR assay protocol fields';

ALTER TABLE cherry_pick_request ADD COLUMN assay_protocol_comments text;
ALTER TABLE cherry_pick_request ADD COLUMN cherry_pick_assay_protocols_followed text;
ALTER TABLE cherry_pick_request ADD COLUMN cherry_pick_followup_results_status text;

COMMIT;
