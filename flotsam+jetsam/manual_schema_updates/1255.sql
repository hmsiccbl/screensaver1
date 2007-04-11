ALTER TABLE cherry_pick_request ADD cherry_pick_request_number INT;
UPDATE cherry_pick_request SET cherry_pick_request_number = ordinal;
ALTER TABLE cherry_pick_request ALTER COLUMN cherry_pick_request_number SET NOT NULL;
ALTER TABLE cherry_pick_request DROP COLUMN ordinal;

ALTER TABLE screen ADD all_time_cherry_pick_request_count INT;
UPDATE screen SET all_time_cherry_pick_request_count = (SELECT COUNT(*) from cherry_pick_request WHERE cherry_pick_request.screen_id = screen.screen_id);
ALTER TABLE screen ALTER COLUMN all_time_cherry_pick_request_count SET NOT NULL;