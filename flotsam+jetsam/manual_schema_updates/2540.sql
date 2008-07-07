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

/* fix data (heuristc!) */
/*update screening_room_user set lab_head_id = (select lab_head_id from screening_room_user sru2 where sru2.screensaver_user_id = screening_room_user.lab_head_id) where exists (select * from screening_room_user sru3 where sru3.screensaver_user_id = screening_room_user.lab_head_id and sru3.user_classification <> 'Principal Investigator');*/


INSERT INTO lab_head (screensaver_user_id, lab_affiliation_id) SELECT screensaver_user_id, lab_affiliation_id FROM screening_room_user WHERE user_classification = 'Principal Investigator';

ALTER TABLE screening_room_user DROP CONSTRAINT fk_screening_room_user_to_lab_head;
ALTER TABLE screening_room_user 
    ADD CONSTRAINT fk_screening_room_user_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screen DROP CONSTRAINT fk_screen_to_lab_head;

ALTER TABLE screen ALTER COLUMN lab_head_id DROP NOT NULL;

/* fix data (heuristc!) */
/*update screen set lab_head_id = null where lab_head_id is not null and exists (select screensaver_user_id from screening_room_user where screensaver_user_id = screen.lab_head_id and user_classification <> 'Principal Investigator' );*/

ALTER TABLE screen 
    ADD CONSTRAINT fk_screen_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screening_room_user DROP COLUMN lab_affiliation_id;

COMMIT;
