BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2536,
current_timestamp,
'renamed screensaver_user_role_type to screensaver_user_role; replaced guestUser role with screeningRoomUser role';

ALTER TABLE screensaver_user_role_type RENAME TO screensaver_user_role;
DELETE FROM screensaver_user_role WHERE screensaver_user_role = 'guestUser' AND screensaver_user_id IN (SELECT screensaver_user_id FROM screensaver_user_role WHERE screensaver_user_role = 'screeningRoomUser');
UPDATE screensaver_user_role SET screensaver_user_role = 'screeningRoomUser' WHERE screensaver_user_role = 'guestUser';
UPDATE screensaver_user_role SET screensaver_user_role = 'cherryPickRequestsAdmin' WHERE screensaver_user_role = 'cherryPickAdmin';
UPDATE screensaver_user_role SET screensaver_user_role = 'smallMoleculeScreeningRoomUser' WHERE screensaver_user_role = 'compoundScreeningRoomUser';


COMMIT;
