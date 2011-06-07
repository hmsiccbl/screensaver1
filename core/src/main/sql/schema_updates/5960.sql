BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5960,
current_timestamp,
'add copy.primary_plate_mg_ml_concentration, copy.primary_plate_molar_concentration, ';

alter table copy add column primary_plate_mg_ml_concentration numeric(4, 1);
alter table copy add column primary_plate_molar_concentration numeric(12, 9);

update copy set primary_plate_mg_ml_concentration = 
(select primary_plate_mg_ml_concentration from (select count(*) as cnt, p.mg_ml_concentration as primary_plate_mg_ml_concentration from plate p where p.copy_id = copy.copy_id group by p.mg_ml_concentration order by count(*) desc limit 1) as x);
/** note: some plates have null values, so not all copies will be non-null 
alter table copy alter column primary_plate_mg_ml_concentration set not null;
**/

update copy set primary_plate_molar_concentration = 
(select primary_plate_molar_concentration from (select count(*) as cnt, p.molar_concentration as primary_plate_molar_concentration from plate p where p.copy_id = copy.copy_id group by p.molar_concentration order by count(*) desc limit 1) as x);
/** note: some plates have null values, so not all copies will be non-null 
alter table copy alter column primary_plate_molar_concentration set not null;
**/
COMMIT;
