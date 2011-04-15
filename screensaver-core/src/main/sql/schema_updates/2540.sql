BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2540,
current_timestamp,
'added LabHead entity type';

CREATE TABLE lab_head (
    screensaver_user_id INT4 NOT NULL,
    lab_affiliation_id TEXT,
    PRIMARY KEY (screensaver_user_id)
);

ALTER TABLE lab_head 
    ADD CONSTRAINT fk_lab_head_to_screening_room_user
    FOREIGN KEY (screensaver_user_id) 
    REFERENCES screening_room_user;

ALTER TABLE lab_head 
    ADD CONSTRAINT fk_lab_head_to_lab_affiliation 
    FOREIGN KEY (lab_affiliation_id) 
    REFERENCES lab_affiliation;

/*  clear lab heads, since existing data violates new lab head constraints; will be updated by ScreenDBSynchronizer */
/* UPDATE screening_room_user SET lab_head_id = NULL; */

INSERT INTO lab_head (screensaver_user_id, lab_affiliation_id) SELECT screensaver_user_id, lab_affiliation_id FROM screening_room_user WHERE user_classification = 'Principal Investigator';

ALTER TABLE screening_room_user DROP CONSTRAINT fk_screening_room_user_to_lab_head;
ALTER TABLE screening_room_user 
    ADD CONSTRAINT fk_screening_room_user_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screen DROP CONSTRAINT fk_screen_to_lab_head;

ALTER TABLE screen ALTER COLUMN lab_head_id DROP NOT NULL;

/*  clear lab heads, since existing data violates new lab head constraints; will be updated by ScreenDBSynchronizer */
/* UPDATE screen SET lab_head_id = NULL; */

ALTER TABLE screen 
    ADD CONSTRAINT fk_screen_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screening_room_user DROP COLUMN lab_affiliation_id;

COMMIT;
