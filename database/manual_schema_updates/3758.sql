BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3758,
current_timestamp,
'convert rna_cherry_pick_screening to more generic cherry_pick_screening; remove rna_cherry_pick_request.asay_protocol';


CREATE TABLE cherry_pick_screening (
  activity_id int4 NOT NULL,
  cherry_pick_request_id int4 NOT NULL,
  primary key (activity_id)
);
INSERT INTO cherry_pick_screening (activity_id, cherry_pick_request_id) 
SELECT activity_id, cherry_pick_request_id FROM rnai_cherry_pick_screening;

ALTER TABLE cherry_pick_screening
  ADD CONSTRAINT fk_cherry_pick_screening_to_activity foreign key (activity_id) references screening;

ALTER TABLE cherry_pick_screening
  ADD CONSTRAINT fk_cherry_pick_screening_to_cherry_pick_request foreign key (cherry_pick_request_id) references cherry_pick_request;

DROP TABLE rnai_cherry_pick_screening;

ALTER TABLE rnai_cherry_pick_request
  DROP COLUMN assay_protocol;

COMMIT;