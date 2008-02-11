BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2050,
current_timestamp,
'added lcp.number_unfulfilled_lab_cherry_picks';

ALTER TABLE cherry_pick_request ADD COLUMN number_unfulfilled_lab_cherry_picks int4;
/* lcp is unfulfilled if it has 0 well volume adjustments && cpap.cplt.status != canceled */
UPDATE cherry_pick_request SET number_unfulfilled_lab_cherry_picks = 
   (SELECT count(*)
   FROM lab_cherry_pick lcp 
   LEFT JOIN well_volume_adjustment wva using(lab_cherry_pick_id) 
   WHERE cherry_pick_request.cherry_pick_request_id = lcp.cherry_pick_request_id 
   AND wva.well_volume_adjustment_id is null)
   -
   (SELECT count(*) 
   FROM lab_cherry_pick lcp 
   JOIN cherry_pick_assay_plate cpap USING(cherry_pick_assay_plate_id)
   JOIN cherry_pick_liquid_transfer cplt
   ON(cpap.cherry_pick_liquid_transfer_id=cplt.activity_id) 
   WHERE cherry_pick_request.cherry_pick_request_id = lcp.cherry_pick_request_id 
   AND cplt.status = 'canceled');
ALTER TABLE cherry_pick_request ALTER COLUMN number_unfulfilled_lab_cherry_picks SET NOT NULL;

COMMIT;