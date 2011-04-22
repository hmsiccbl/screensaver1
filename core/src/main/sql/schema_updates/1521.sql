BEGIN;

DROP TABLE screensaver_revision;
CREATE TABLE schema_history (
  screensaver_revision INT4,
  date_updated TIMESTAMP,
  comment text,
  PRIMARY KEY (screensaver_revision)
);
INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1521, 
current_timestamp, 
'added tables: activity (new parent of screening_room_activity), administrative_activity, well_volume_correction_activity, well_volume_adjustment';
 
CREATE TABLE activity (
    activity_id INT4 NOT NULL,
    version INT4 NOT NULL,
    date_created TIMESTAMP NOT NULL,
    date_of_activity TIMESTAMP NOT NULL,
    comments TEXT,
    performed_by_id INT4 NOT NULL,
    PRIMARY KEY (activity_id)
);

CREATE TABLE administrative_activity (
    activity_id INT4 NOT NULL,
    approved_by_id int4,
    PRIMARY KEY (activity_id)
);

ALTER TABLE cherry_pick_liquid_transfer ADD COLUMN activity_id int4;
UPDATE cherry_pick_liquid_transfer SET activity_id = screening_room_activity_id;
ALTER TABLE cherry_pick_liquid_transfer DROP COLUMN screening_room_activity_id CASCADE;
ALTER TABLE cherry_pick_liquid_transfer ALTER COLUMN activity_id SET NOT NULL;
ALTER TABLE cherry_pick_liquid_transfer ADD CONSTRAINT cherry_pick_liquid_transfer_pkey PRIMARY KEY (activity_id);

ALTER TABLE library_screening ADD COLUMN activity_id int4;
UPDATE library_screening SET activity_id = screening_room_activity_id;
ALTER TABLE library_screening DROP COLUMN screening_room_activity_id CASCADE;
ALTER TABLE library_screening ALTER COLUMN activity_id SET NOT NULL;
ALTER TABLE library_screening ADD CONSTRAINT library_screening_pkey PRIMARY KEY (activity_id);

ALTER TABLE rnai_cherry_pick_screening ADD COLUMN activity_id int4;
UPDATE rnai_cherry_pick_screening SET activity_id = screening_room_activity_id;
ALTER TABLE rnai_cherry_pick_screening DROP COLUMN screening_room_activity_id CASCADE;
ALTER TABLE rnai_cherry_pick_screening ALTER COLUMN activity_id SET NOT NULL;
ALTER TABLE rnai_cherry_pick_screening ADD CONSTRAINT rnai_cherry_pick_screening_pkey PRIMARY KEY (activity_id);

ALTER TABLE screening ADD COLUMN activity_id int4;
UPDATE screening SET activity_id = screening_room_activity_id;
ALTER TABLE screening DROP COLUMN screening_room_activity_id CASCADE;
ALTER TABLE screening ALTER COLUMN activity_id SET NOT NULL;
ALTER TABLE screening ADD CONSTRAINT screening_pkey PRIMARY KEY (activity_id);

ALTER TABLE screening_room_activity ADD COLUMN activity_id int4;
UPDATE screening_room_activity SET activity_id = screening_room_activity_id;
ALTER TABLE screening_room_activity DROP COLUMN screening_room_activity_id CASCADE;
ALTER TABLE screening_room_activity ALTER COLUMN activity_id SET NOT NULL;
ALTER TABLE screening_room_activity ADD CONSTRAINT screening_room_activity_pkey PRIMARY KEY (activity_id);

INSERT INTO activity (activity_id, version, date_created, date_of_activity, comments, performed_by_id) 
SELECT activity_id, version, date_created, date_of_activity, comments, performed_by_id 
FROM screening_room_activity;

ALTER TABLE screening_room_activity DROP COLUMN version;
ALTER TABLE screening_room_activity DROP COLUMN date_created;
ALTER TABLE screening_room_activity DROP COLUMN date_of_activity;
ALTER TABLE screening_room_activity DROP COLUMN comments;
ALTER TABLE screening_room_activity DROP COLUMN performed_by_id;

ALTER TABLE activity 
    ADD CONSTRAINT fk_activity_to_performed_by 
    FOREIGN KEY (performed_by_id) 
    REFERENCES screensaver_user;

ALTER TABLE administrative_activity 
    ADD CONSTRAINT FKF20C1700702E36D6 
    FOREIGN KEY (activity_id) 
    REFERENCES activity;

ALTER TABLE administrative_activity 
    ADD CONSTRAINT fk_activity_to_administrator_user 
    FOREIGN KEY (approved_by_id) 
    REFERENCES administrator_user;

ALTER TABLE cherry_pick_liquid_transfer 
    ADD CONSTRAINT FKE177A65670357948 
    FOREIGN KEY (activity_id) 
    REFERENCES screening_room_activity;

ALTER TABLE library_screening 
    ADD CONSTRAINT FK62CFA172D75A3C9E 
    FOREIGN KEY (activity_id) 
    REFERENCES screening;

ALTER TABLE rnai_cherry_pick_screening 
    ADD CONSTRAINT FK86F54223D75A3C9E 
    FOREIGN KEY (activity_id) 
    REFERENCES screening;

ALTER TABLE screening 
    ADD CONSTRAINT FK774EFF670357948 
    FOREIGN KEY (activity_id) 
    REFERENCES screening_room_activity;

ALTER TABLE screening_room_activity 
    ADD CONSTRAINT FKB04C056A702E36D6 
    FOREIGN KEY (activity_id) 
    REFERENCES activity;

ALTER TABLE screening_room_activity_id_seq RENAME TO activity_id_seq;

CREATE TABLE well_volume_adjustment (
    well_volume_adjustment_id INT4 NOT NULL,
    version INT4 NOT NULL,
    copy_id VARCHAR(255) NOT NULL,
    well_id VARCHAR(255) NOT NULL,
    microliter_volume NUMERIC(19, 2),
    lab_cherry_pick_id INT4,
    well_volume_correction_activity_id INT4,
    PRIMARY KEY (well_volume_adjustment_id)
);

CREATE TABLE well_volume_correction_activity (
    activity_id INT4 NOT NULL,
    PRIMARY KEY (activity_id)
);

ALTER TABLE well_volume_adjustment 
    ADD CONSTRAINT fk_well_volume_adjustment_to_copy 
    FOREIGN KEY (copy_id) 
    REFERENCES copy;

ALTER TABLE well_volume_adjustment 
    ADD CONSTRAINT fk_well_volume_adjustment_to_well 
    FOREIGN KEY (well_id) 
    REFERENCES well;

ALTER TABLE well_volume_adjustment 
    ADD CONSTRAINT FK5BDCFA818B8152E9 
    FOREIGN KEY (lab_cherry_pick_id) 
    REFERENCES lab_cherry_pick;

ALTER TABLE well_volume_adjustment
    ADD CONSTRAINT FK5BDCFA81884D0DE4
    FOREIGN KEY (well_volume_correction_activity_id)
    REFERENCES well_volume_correction_activity;

ALTER TABLE well_volume_correction_activity 
    ADD CONSTRAINT FK33CFD83C15764524 
    FOREIGN KEY (activity_id) 
    REFERENCES administrative_activity;

CREATE SEQUENCE well_volume_adjustment_id_seq;


INSERT INTO well_volume_adjustment (well_volume_adjustment_id, version, copy_id, well_id, microliter_volume, lab_cherry_pick_id) SELECT nextval('well_volume_adjustment_id_seq'), 0, lcp.copy_id, lcp.source_well_id, cpr.microliter_transfer_volume_per_well_approved, lcp.lab_cherry_pick_id FROM lab_cherry_pick lcp join cherry_pick_request cpr using(cherry_pick_request_id) WHERE lcp.copy_id IS NOT NULL;
ALTER TABLE lab_cherry_pick DROP CONSTRAINT fk_lab_cherry_pick_to_copy;
ALTER TABLE lab_cherry_pick DROP COLUMN copy_id;

COMMIT;