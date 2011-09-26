BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6358,
current_timestamp,
'change molar concentration type to numeric(13,12) for plate, lab_activity, copy, and well, add new fields for [#2920] well concentration';

alter table copy rename column primary_plate_molar_concentration to primary_well_molar_concentration;
alter table copy rename column primary_plate_mg_ml_concentration to primary_well_mg_ml_concentration;
alter table copy alter column primary_well_molar_concentration type numeric(13,12);
alter table copy alter column primary_well_mg_ml_concentration type numeric(5,3);
alter table copy add column min_molar_concentration  numeric(13,12);
alter table copy add column max_molar_concentration numeric(13,12);
alter table copy add column min_mg_ml_concentration numeric(5,3);
alter table copy add column max_mg_ml_concentration numeric(5,3);
alter table copy add column well_concentration_dilution_factor numeric(8,2) default 1.0;  /* NOTE: this allows up to 3 digits, or 999 */

alter table plate add column min_molar_concentration  numeric(13,12);
alter table plate add column max_molar_concentration numeric(13,12);
alter table plate add column min_mg_ml_concentration numeric(5,3);
alter table plate add column max_mg_ml_concentration numeric(5,3);
alter table plate add column primary_well_molar_concentration  numeric(13,12);
alter table plate add column primary_well_mg_ml_concentration numeric(5,3);
/** alter table plate add column well_concentration_dilution_factor numeric(8,2) default 1.0; */ /* NOTE: this allows up to 3 digits, or 999 */

alter table well alter column molar_concentration type numeric(13,12);
alter table well add column mg_ml_concentration numeric(5,3);

alter table lab_activity alter column molar_concentration type numeric(13,12);

/** TODO: if necessary, perform facility specific migration of concentration values from plate to well and copy entities **/

commit;