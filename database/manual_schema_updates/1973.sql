BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1973,
current_timestamp,
'CPR requested empty rows';

CREATE TABLE cherry_pick_request_requested_empty_row (
    cherry_pick_request_id INT4 NOT NULL,
    requested_empty_row CHAR(1) NOT NULL,
    PRIMARY KEY (cherry_pick_request_id, requested_empty_row)
);

ALTER TABLE cherry_pick_request_requested_empty_row 
    ADD CONSTRAINT fk_cherry_pick_request_requested_empty_row_to_cherry_pick_request 
    FOREIGN KEY (cherry_pick_request_id) 
    REFERENCES cherry_pick_request;

COMMIT;