BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1334;

ALTER TABLE cherry_pick_request ADD COLUMN date_volume_approved timestamp;
ALTER TABLE cherry_pick_request ADD COLUMN volume_approved_by_id int4;
ALTER TABLE cherry_pick_request ADD CONSTRAINT fk_cherry_pick_request_to_volume_approved_by 
  FOREIGN KEY (volume_approved_by_id) 
  REFERENCES administrator_user;

COMMIT;
