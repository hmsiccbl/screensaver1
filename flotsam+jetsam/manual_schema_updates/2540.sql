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

/* find users with non-PI lab heads */
/* select distinct sru.* from screening_room_user sru join screening_room_user lh on(sru.lab_head_id=lh.screensaver_user_id) where lh.user_classification <> 'Principal Investigator'; */

/* fix users with non-PI lab heads (heuristic!) */
/* update screening_room_user set lab_head_id = (select lab_head_id from screening_room_user bad_lh where bad_lh.screensaver_user_id = screening_room_user.lab_head_id) where exists (select * from screening_room_user bad_lh where bad_lh.screensaver_user_id = screening_room_user.lab_head_id and bad_lh.user_classification <> 'Principal Investigator'); */


/* find PIs with lab heads */
/* select * from screening_room_user where lab_head_id is not null and user_classification = 'Principal Investigator'; */

/* fix PIs with lab heads */
/* update screening_room_user set lab_head_id = null where lab0_head_id = screensaver_user_id; 
   update screening_room_user set lab_head_id = null where user_classification = 'Principal Investigator' and lab_head_id is not null; */


INSERT INTO lab_head (screensaver_user_id, lab_affiliation_id) SELECT screensaver_user_id, lab_affiliation_id FROM screening_room_user WHERE user_classification = 'Principal Investigator';

ALTER TABLE screening_room_user DROP CONSTRAINT fk_screening_room_user_to_lab_head;
ALTER TABLE screening_room_user 
    ADD CONSTRAINT fk_screening_room_user_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screen DROP CONSTRAINT fk_screen_to_lab_head;

ALTER TABLE screen ALTER COLUMN lab_head_id DROP NOT NULL;

/* find bad screen.labHead */
/* select * from screen s join screening_room_user lh on(s.lab_head_id=lh.screensaver_user_id) where lh.user_classification <> 'Principal Investigator'; */
/* fix screen.labHead, if not a lab head, set to null */
/* update screen set lab_head_id = null where (select user_classification from screening_room_user where screensaver_user_id = screen.lab_head_id) <> 'Principal Investigator'; */

ALTER TABLE screen 
    ADD CONSTRAINT fk_screen_to_lab_head 
    FOREIGN KEY (lab_head_id) 
    REFERENCES lab_head;

ALTER TABLE screening_room_user DROP COLUMN lab_affiliation_id;

COMMIT;


