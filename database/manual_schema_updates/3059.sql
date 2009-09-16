BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3059,
current_timestamp,
'attached_file association with screening_room_user';

ALTER TABLE attached_file ADD COLUMN screensaver_user_id int4;
ALTER TABLE attached_file ALTER COLUMN screen_id DROP NOT NULL;
ALTER TABLE attached_file 
    ADD CONSTRAINT fk_attached_file_to_screening_room_user 
    FOREIGN KEY (screensaver_user_id) 
    REFERENCES screening_room_user;

ALTER TABLE attached_file DROP CONSTRAINT attached_file_screen_id_key;
CREATE UNIQUE INDEX attached_file_unique ON attached_file (screen_id, screensaver_user_id, filename);

COMMIT;
