BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4778,
current_timestamp,
'add the project phase field to screen';

alter table screen add column project_phase text;
update screen set project_phase = 'Primary Screen';
alter table screen alter column project_phase set not null;


COMMIT;
