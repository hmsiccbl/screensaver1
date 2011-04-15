BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2292,
current_timestamp,
'CPR empty wells now explicitly maintains the previous required empty edge rows and columns';

ALTER TABLE cherry_pick_request_requested_empty_well RENAME TO cherry_pick_request_empty_well;
/*ALTER TABLE cherry_pick_request_empty_well DROP CONSTRAINT fk_cherry_pick_request_requested_empty_wells_to_cherry_pick_request;*/
ALTER TABLE cherry_pick_request_empty_well 
    ADD CONSTRAINT fk_cherry_pick_request_empty_well_to_cherry_pick_request 
    FOREIGN KEY (cherry_pick_request_id) 
    REFERENCES cherry_pick_request;

/* Add the explicit outer 2 rows and columns to RNAi CPRs' 'empty wells' */

CREATE TEMP TABLE tmp_rows (row_name CHAR(1));
INSERT INTO tmp_rows VALUES ('A');
INSERT INTO tmp_rows VALUES ('B');
INSERT INTO tmp_rows VALUES ('C');
INSERT INTO tmp_rows VALUES ('D');
INSERT INTO tmp_rows VALUES ('E');
INSERT INTO tmp_rows VALUES ('F');
INSERT INTO tmp_rows VALUES ('G');
INSERT INTO tmp_rows VALUES ('H');
INSERT INTO tmp_rows VALUES ('I');
INSERT INTO tmp_rows VALUES ('J');
INSERT INTO tmp_rows VALUES ('K');
INSERT INTO tmp_rows VALUES ('L');
INSERT INTO tmp_rows VALUES ('M');
INSERT INTO tmp_rows VALUES ('N');
INSERT INTO tmp_rows VALUES ('O');
INSERT INTO tmp_rows VALUES ('P');
CREATE TEMP TABLE tmp_columns (column_name CHAR(2));
INSERT INTO tmp_columns VALUES ('01');
INSERT INTO tmp_columns VALUES ('02');
INSERT INTO tmp_columns VALUES ('03');
INSERT INTO tmp_columns VALUES ('04');
INSERT INTO tmp_columns VALUES ('05');
INSERT INTO tmp_columns VALUES ('06');
INSERT INTO tmp_columns VALUES ('07');
INSERT INTO tmp_columns VALUES ('08');
INSERT INTO tmp_columns VALUES ('09');
INSERT INTO tmp_columns VALUES ('10');
INSERT INTO tmp_columns VALUES ('11');
INSERT INTO tmp_columns VALUES ('12');
INSERT INTO tmp_columns VALUES ('13');
INSERT INTO tmp_columns VALUES ('14');
INSERT INTO tmp_columns VALUES ('15');
INSERT INTO tmp_columns VALUES ('16');
INSERT INTO tmp_columns VALUES ('17');
INSERT INTO tmp_columns VALUES ('18');
INSERT INTO tmp_columns VALUES ('19');
INSERT INTO tmp_columns VALUES ('20');
INSERT INTO tmp_columns VALUES ('21');
INSERT INTO tmp_columns VALUES ('22');
INSERT INTO tmp_columns VALUES ('23');
INSERT INTO tmp_columns VALUES ('24');
INSERT INTO cherry_pick_request_empty_well (cherry_pick_request_id, well_name) SELECT cpr.cherry_pick_request_id, well_name FROM cherry_pick_request cpr JOIN rnai_cherry_pick_request rcpr using(cherry_pick_request_id), (SELECT DISTINCT row_name || column_name as well_name FROM tmp_rows r, tmp_columns c WHERE r.row_name IN ('A', 'B', 'O', 'P') OR c.column_name IN ('01', '02', '23', '24')) AS empty_well_names;

COMMIT;
