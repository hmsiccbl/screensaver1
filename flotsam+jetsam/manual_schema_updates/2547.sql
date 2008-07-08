BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2547,
current_timestamp,
'reinstated screen.labHead not null';

/* fix data (heuristic) */
/*UPDATE screen SET lab_head_id = (SELECT lab_head_id FROM screening_room_user WHERE screensaver_user_id = screen.lead_screener_id) WHERE lab_head_id is null;*/

ALTER TABLE screen ALTER COLUMN lab_head_id SET NOT NULL;

COMMIT;
