BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2272,
current_timestamp,
'added cpr.cherryPickAssayPlateType';

ALTER TABLE cherry_pick_request ADD COLUMN assay_plate_type text;
UPDATE cherry_pick_request set assay_plate_type = (SELECT MIN(assay_plate_type) FROM cherry_pick_assay_plate cpap WHERE cpap.cherry_pick_request_id = cherry_pick_request.cherry_pick_request_id);

UPDATE cherry_pick_request set assay_plate_type = 'Eppendorf'
FROM screen s
WHERE s.screen_id = cherry_pick_request.screen_id AND s.screen_type = 'RNAi' AND cherry_pick_request.assay_plate_type is null;

UPDATE cherry_pick_request set assay_plate_type = 'ABgene'
FROM screen s
WHERE s.screen_id = cherry_pick_request.screen_id AND s.screen_type = 'Small Molecule' AND cherry_pick_request.assay_plate_type is null;

ALTER TABLE cherry_pick_request ALTER COLUMN assay_plate_type SET NOT NULL;

COMMIT;
