BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2304,
current_timestamp,
'renamed screening_room_activity to lab_activity';

ALTER TABLE equipment_used DROP CONSTRAINT fk_equipment_used_to_screening_room_activity;
ALTER TABLE cherry_pick_liquid_transfer DROP CONSTRAINT fk_cherry_pick_liquid_transfer_to_activity;
ALTER TABLE screening DROP CONSTRAINT fk_screening_to_activity;
ALTER TABLE screening_room_activity DROP CONSTRAINT fk_screening_room_activity_to_screen;
ALTER TABLE screening_room_activity DROP CONSTRAINT fk_screening_room_activity_to_activity;

ALTER TABLE screening_room_activity RENAME TO lab_activity;
ALTER TABLE lab_activity 
    ADD CONSTRAINT fk_lab_activity_to_screen 
    FOREIGN KEY (screen_id) 
    REFERENCES screen;
ALTER TABLE lab_activity 
    ADD CONSTRAINT fk_lab_activity_to_activity 
    FOREIGN KEY (activity_id) 
    REFERENCES activity;
/*ALTER INDEX screening_room_activity_pkey RENAME TO lab_activity_pkey;*/

ALTER TABLE equipment_used RENAME COLUMN screening_room_activity_id TO lab_activity_id;
ALTER TABLE equipment_used 
    ADD CONSTRAINT fk_equipment_used_to_lab_activity 
    FOREIGN KEY (lab_activity_id) 
    REFERENCES lab_activity;
ALTER TABLE cherry_pick_liquid_transfer 
    ADD CONSTRAINT fk_cherry_pick_liquid_transfer_to_activity 
    FOREIGN KEY (activity_id) 
    REFERENCES lab_activity;
ALTER TABLE screening 
    ADD CONSTRAINT fk_screening_to_activity 
    FOREIGN key (activity_id) 
    REFERENCES lab_activity;

COMMIT;
