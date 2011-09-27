BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6360,
current_timestamp,
'remove obsolete plate concentration fields';

alter table plate drop column mg_ml_concentration;
alter table plate drop column molar_concentration;

commit;