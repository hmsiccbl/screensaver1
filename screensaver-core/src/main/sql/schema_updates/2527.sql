BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2527,
current_timestamp,
'removed screeningRoomUser.isNonScreeningUser';

ALTER TABLE screening_room_user DROP COLUMN is_non_screening_user;

COMMIT;
