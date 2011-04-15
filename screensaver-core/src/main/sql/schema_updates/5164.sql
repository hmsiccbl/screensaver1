BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5164,
current_timestamp,
'rename concentration fields to molar_concentration';

alter table lab_activity rename concentration to molar_concentration;
alter table plate rename concentration to molar_concentration;


COMMIT;
