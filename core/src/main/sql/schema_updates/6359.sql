BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6359,
current_timestamp,
'change molar concentration type to numeric(13,12) for plate, lab_activity, copy, and well, add new fields for [#2920] well concentration';

/** TODO: this is an ICCB-L specific data migration script to be performed after schema updates are applied for the ticket:
 * [#2920] - store concentration values at the well level
 * This script migrates concentration values from the plate to the new values on the well, it creates derived aggregate (min/max/primary) concentration
 * values on the plate and the copy entity, and it calculates a dilution factor that will be stored on the copy.
 * 
 * PRECONDITION 1: All concentration values for the plates within a copy are the same value.  (the new schema enforces this).
 * This script will fail if all concentration values for the plates within a copy are the same value.  Use this sql to determine:
 * select * from (select copy_id, count(distinct mg_ml_concentration) from plate join copy using(copy_id) where mg_ml_concentration is not null group by copy_id) as a where count > 1;
 * and,
 * select * from (select copy_id, count(distinct molar_concentration) from plate join copy using(copy_id) where molar_concentration is not null group by copy_id) as a where count > 1;
 * 
 * PRECONDITION 2: Current plates have either a mg_ml_concentration value, or a molar_concentration value, but not both.
 * This script will not fail if this condition is not met, however, it will arbitrarily use the molar_concentration value in conflicting records (due to ordering).
 * use this sql to verify:
 * select plate_number, c.name, short_name, mg_ml_concentration, molar_concentration from plate p join copy c using(copy_id) join library l using(library_id) where mg_ml_concentration is not null and molar_concentration is not null;
 * 
 * Well concentrations are set to the maximum concentration found for all plates in a library, 
 * the copy dilution factor is determined by the ratio (max concentration in the library / concentration of the copy plates ).
 */

/** perform the migration **/
    
/** THIS COMMAND WILL FAIL IF PRECONDITION 1 LISTED ABOVE IS NOT MET **/
update copy c set primary_well_mg_ml_concentration = (select distinct(mg_ml_concentration) from plate join copy c1 using(copy_id) where c1.copy_id = c.copy_id and plate.mg_ml_concentration is not null );
update copy c set well_concentration_dilution_factor =  
  (select max(primary_well_mg_ml_concentration) from copy c1 where c1.library_id = c.library_id) / primary_well_mg_ml_concentration where primary_well_mg_ml_concentration is not null;
update copy c set primary_well_mg_ml_concentration = (select max(primary_well_mg_ml_concentration) from copy c1 where c1.library_id = c.library_id);  
update copy set max_mg_ml_concentration = primary_well_mg_ml_concentration;
update copy set min_mg_ml_concentration = primary_well_mg_ml_concentration;

  
/** THIS COMMAND WILL FAIL IF PRECONDITION 1 LISTED ABOVE IS NOT MET **/
update copy c set primary_well_molar_concentration = (select distinct(molar_concentration) from plate join copy c1 using(copy_id) where c1.copy_id = c.copy_id and plate.molar_concentration is not null );
update copy c set well_concentration_dilution_factor =  
  (select max(primary_well_molar_concentration) from copy c1 where c1.library_id = c.library_id) / primary_well_molar_concentration where primary_well_molar_concentration is not null;
update copy c set primary_well_molar_concentration = (select max(primary_well_molar_concentration) from copy c1 where c1.library_id = c.library_id);  
update copy set max_molar_concentration = primary_well_molar_concentration;
update copy set min_molar_concentration = primary_well_molar_concentration;

  
update plate p set primary_well_mg_ml_concentration = 
  (select max(c.primary_well_mg_ml_concentration) from copy c join plate p1 using(copy_id) where p1.plate_id=p.plate_id) where mg_ml_concentration is not null;
update plate set min_mg_ml_concentration = primary_well_mg_ml_concentration;
update plate set max_mg_ml_concentration = primary_well_mg_ml_concentration;
  
update plate p set primary_well_molar_concentration = 
  (select max(c.primary_well_molar_concentration) from copy c  join plate p1 using(copy_id) where p1.plate_id=p.plate_id) where molar_concentration is not null;
update plate set min_molar_concentration = primary_well_molar_concentration;
update plate set max_molar_concentration = primary_well_molar_concentration;
 

/** set the well value to the max mg/ml concentratoin for all the plates in all the copies **/
create temp table plate_concentration as select plate_number, max(primary_well_mg_ml_concentration) from plate 
  where primary_well_mg_ml_concentration is not null group by plate_number;
update well set mg_ml_concentration = (select max from plate_concentration pc where pc.plate_number = well.plate_number)
  where well.plate_number in(select plate_number from plate_concentration) 
  and well.library_well_type = 'experimental';
drop table plate_concentration;

/** set the well value to the max molar concentration for all the plates in all the copies **/
create temp table plate_concentration as select plate_number, max(primary_well_molar_concentration) from plate 
  where primary_well_molar_concentration is not null group by plate_number;
update well set molar_concentration = (select max from plate_concentration pc where pc.plate_number = well.plate_number)
  where well.plate_number in(select plate_number from plate_concentration)
  and well.library_well_type = 'experimental';
drop table plate_concentration;

COMMIT;
