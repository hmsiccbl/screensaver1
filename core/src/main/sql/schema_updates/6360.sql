BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6358,
current_timestamp,
'[#2920] well concentration post migration script, remove old plate concentration fields';

alter table plate drop column mg_ml_concentration;
alter table plate drop column molar_concentration;

commit;