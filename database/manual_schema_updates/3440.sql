BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3440,
current_timestamp,
'remove unique index on lab_cherry_pick';

/* ICCB-L data never actually was given this constraint; but including here, commented-out, for other deployments */
/*alter table lab_cherry_pick drop constraint lab_cherry_pick_cherry_pick_request_id_key;*/

COMMIT;