/** 
 * For [#3277] add "perturbagen concentration" field to screen
 */
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6735,
current_timestamp,
'add perturbagen_molar_concentration, perturbagen_mg_ml_concentration to screen';

alter table screen add column  perturbagen_molar_concentration numeric(13, 12);
alter table screen add column  perturbagen_ug_ml_concentration numeric(5, 3);

COMMIT;