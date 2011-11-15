/** 
 * For [#3279] Add “N/A” option to cell line vocabulary for SM
 */
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6765,
current_timestamp,
'add "N/A" option to the cell line selections';

insert into cell_line (cell_line_id, value, version) values (nextval('cell_line_id_seq'), 'N/A', 0);
/**
 * To be approved
insert into cell_line (cell_line_id, value, version) values (nextval('cell_line_id_seq'), 'Bacteria', 0);
insert into cell_line (cell_line_id, value, version) values (nextval('cell_line_id_seq'), 'Yeast', 0);
**/
COMMIT;