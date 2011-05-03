BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5165,
current_timestamp,
'add mg_ml_concentration to plate';

alter table plate add mg_ml_concentration numeric(4, 1);

COMMIT;
