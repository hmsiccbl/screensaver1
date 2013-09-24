/* ICCB-L specific!  Modify as necessary for your facility's data */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20130112,
current_timestamp,
'Issue #112 Attach multiple cell lines to a screen';

create table screen_cell_line (
    screen_id int4 not null,
    cell_line_id int4 not null,
    primary key (screen_id, cell_line_id)
);

alter table screen_cell_line
    add constraint fk_screen_cell_line_to_screen
    foreign key (screen_id)
    references screen;

alter table screen_cell_line
    add constraint fk_screen_cell_line_to_cell
    foreign key (cell_line_id)
    references cell_line;

insert into screen_cell_line (screen_id, cell_line_id)  select screen_id, cell_line_id from screen where cell_line_id is not null;

alter table screen drop column cell_line_id;

COMMIT;