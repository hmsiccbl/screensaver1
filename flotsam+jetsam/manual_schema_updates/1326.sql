BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1326;

/* since all of this data is currently imported via ScreenDBSynchronizer, */
/* its okay to just delete it all, and this is the easiest way to update the schema */
DELETE FROM lab_cherry_pick;
DELETE FROM screener_cherry_pick;
DELETE FROM cherry_pick_assay_plate;
DELETE FROM rnai_cherry_pick_request;
DELETE FROM compound_cherry_pick_request;
DELETE FROM cherry_pick_request;
DELETE FROM plates_used;
DELETE FROM library_screening;
DELETE FROM rnai_cherry_pick_screening;
DELETE FROM screening;
DELETE FROM equipment_used;
DELETE FROM cherry_pick_liquid_transfer;
DELETE FROM screening_room_activity;

ALTER SEQUENCE cherry_pick_request_id_seq RESTART WITH 10000;
ALTER TABLE cherry_pick_request RENAME COLUMN cherry_pick_request_number TO legacy_cherry_pick_request_number;
ALTER TABLE cherry_pick_request ALTER COLUMN legacy_cherry_pick_request_number DROP NOT NULL;
ALTER TABLE cherry_pick_request ADD COLUMN ordinal int4;
ALTER TABLE cherry_pick_request ALTER COLUMN ordinal SET NOT NULL;

COMMIT;
