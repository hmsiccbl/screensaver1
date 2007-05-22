BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1362;
 
ALTER TABLE screening_room_user ADD COLUMN lab_name text;
UPDATE screening_room_user SET lab_name = ssu.last_name || ', ' || ssu.first_name || ' - ' || la.affiliation_name FROM lab_affiliation la, screensaver_user ssu WHERE lab_head_id IS NULL and screening_room_user.lab_affiliation_id = la.lab_affiliation_id and ssu.screensaver_user_id = screening_room_user.screensaver_user_id;
UPDATE screening_room_user SET lab_name = ssu.last_name || ', ' || ssu.first_name FROM lab_affiliation la, screensaver_user ssu WHERE lab_head_id IS NULL and screening_room_user.lab_affiliation_id is null and ssu.screensaver_user_id = screening_room_user.screensaver_user_id;

COMMIT;
