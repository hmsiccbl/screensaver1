BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4785,
current_timestamp,
'add project_phase, project_id fields to screen';

alter table screen add column project_phase text;
alter table screen add column project_id text;
update screen set project_phase = 'Primary Screen';
alter table screen alter column project_phase set not null;


COMMIT;
