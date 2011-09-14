BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6429,
current_timestamp,
'add RNAi information fields to screen: species, cell_line, transfection_agent';

alter table screen add column species text;
alter table screen add column cell_line_id int4;
alter table screen add column transfection_agent_id int4;

create table cell_line (
    cell_line_id int4 not null,
    value text not null unique,
    primary key (cell_line_id)
);
create table transfection_agent (
    transfection_agent_id int4 not null,
    value text not null unique,
    primary key (transfection_agent_id)
);

alter table screen 
    add constraint fk_screen_to_cell_line 
    foreign key (cell_line_id) 
    references cell_line;
alter table screen 
    add constraint fk_screen_to_transfection_agent 
    foreign key (transfection_agent_id) 
    references transfection_agent;

create sequence cell_line_id_seq;
create sequence transfection_agent_id_seq;

\copy cell_line from 'cellLines.csv' WITH CSV

select setval('cell_line_id_seq',max(cell_line_id)) from cell_line;

\copy transfection_agent from 'transfectionAgents.csv' WITH CSV

select setval('transfection_agent_id_seq',max(transfection_agent_id)) from transfection_agent;

commit;
