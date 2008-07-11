BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2557,
current_timestamp,
'role updates';

UPDATE screensaver_user_role SET screensaver_user_role = 'screener' WHERE screensaver_user_role = 'screeningRoomUser';
UPDATE screensaver_user_role SET screensaver_user_role = 'smallMoleculeScreener' WHERE screensaver_user_role = 'smallMoleculeScreeningRoomUser';
UPDATE screensaver_user_role SET screensaver_user_role = 'rnaiScreener' WHERE screensaver_user_role = 'rnaiScreeningRoomUser';
INSERT INTO screensaver_user_role (screensaver_user_id, screensaver_user_role) SELECT screensaver_user_id, 'screensaverUser' FROM screensaver_user_role WHERE screensaver_user_role = 'screener';
INSERT INTO screensaver_user_role (screensaver_user_id, screensaver_user_role) SELECT screensaver_user_id, 'screensaverUser' FROM screensaver_user_role WHERE screensaver_user_role = 'readEverythingAdmin';
INSERT INTO screensaver_user_role (screensaver_user_id, screensaver_user_role) SELECT screensaver_user_id, 'screensaverUser' FROM screensaver_user u WHERE NOT EXISTS (SELECT * FROM screensaver_user_role r WHERE r.screensaver_user_id = u.screensaver_user_id);

COMMIT;
