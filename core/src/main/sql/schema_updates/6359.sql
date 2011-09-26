BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6359,
current_timestamp,
'change molar concentration type to numeric(13,12) for plate, lab_activity, copy, and well, add new fields for [#2920] well concentration';

/** NOTE: this is an ICCB-L specific data migration script to be performed after schema updates are applied for the ticket:
 * [#2920]  - store concentration values at the well level
 * This script migrates concentration values from the plate to the new values on the well and the copy entity.
 */

/** all natural product libraries should be initialized to 15 mg/ml per CES, Informatics 2011-09-08 **/
update plate set mg_ml_concentration = 15.0 
  where mg_ml_concentration is null 
  and plate_number in (select plate_number from plate join copy using(copy_id) join library using (library_id) where library_type like 'Natural Products');

/** this fixes an extant error in the data, discussed with Doug on 9/23; there are 12 library plates with _both_ mg/ml and molar concentration values.  In all of these cases, the molar concentration will be used. - sde4 **/
update plate set mg_ml_concentration = null where plate_id in 
  (select plate_id from plate join copy using(copy_id) join library using(library_id) 
    where screen_type = 'Small Molecule' 
    and molar_concentration is not null and mg_ml_concentration is not null);
  
/** perform the migration **/
    
update copy c set primary_well_mg_ml_concentration = (select max(mg_ml_concentration) from plate join copy c1 using(copy_id) where c1.copy_id = c.copy_id );  
update copy c set well_concentration_dilution_factor =  
  (select max(primary_well_mg_ml_concentration) from copy c1 where c1.library_id = c.library_id) / primary_well_mg_ml_concentration where primary_well_mg_ml_concentration is not null;
update copy c set primary_well_mg_ml_concentration = (select max(primary_well_mg_ml_concentration) from copy c1 where c1.library_id = c.library_id);  
update copy set max_mg_ml_concentration = primary_well_mg_ml_concentration;
update copy set min_mg_ml_concentration = primary_well_mg_ml_concentration;

  
update copy c set primary_well_molar_concentration = (select max(molar_concentration) from plate join copy c1 using(copy_id) where c1.copy_id = c.copy_id );  
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
 

/** set the well value to the max for all the plates in all the copies **/
create temp table plate_concentration as select plate_number, max(primary_well_mg_ml_concentration) from plate 
  where primary_well_mg_ml_concentration is not null group by plate_number;
update well set mg_ml_concentration = (select max from plate_concentration pc where pc.plate_number = well.plate_number)
  where well.plate_number in(select plate_number from plate_concentration) 
  and well.library_well_type = 'experimental';
drop table plate_concentration;

/** set the well value to the max for all the plates in all the copies **/
create temp table plate_concentration as select plate_number, max(primary_well_molar_concentration) from plate 
  where primary_well_molar_concentration is not null group by plate_number;
update well set molar_concentration = (select max from plate_concentration pc where pc.plate_number = well.plate_number)
  where well.plate_number in(select plate_number from plate_concentration)
  and well.library_well_type = 'experimental';
drop table plate_concentration;

COMMIT;
