/* manual schema upgrade for schema-upgrade-2007                               */

/* this schema upgrade is designed to work on postgres 8.2.4 or greater. it    */
/* has not been tested on earlier versions, and probably will not work.        */

/* this schema upgrade addresses differences in the schema that came about     */
/* due to migrating from XDoclet to Hibernate Annotations. most of the changes */
/* are simply standardization fixes, such as renaming positive to is_positive, */
/* fixing nullability of fields, plus some more special-case situations. in a  */
/* couple of places, the schema has actually devolved, since we were not able  */
/* to get hibernate annotations to do what we wanted. there is still hope for  */
/* these, but they will be addressed at a later point in time.                 */

/* schema changes here are alphabetical by table.                              */

BEGIN TRANSACTION;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 1929, current_timestamp, 'schema-upgrade-2007';

ALTER TABLE administrative_activity ADD COLUMN date_approved TIMESTAMP;

ALTER TABLE annotation_type ALTER COLUMN name TYPE TEXT;
ALTER TABLE annotation_type RENAME COLUMN numeric TO is_numeric;
ALTER TABLE annotation_type ALTER COLUMN ordinal SET NOT NULL;

ALTER TABLE annotation_value ALTER COLUMN vendor_identifier TYPE TEXT;
ALTER TABLE annotation_value ALTER COLUMN vendor_name TYPE TEXT;

ALTER TABLE billing_information RENAME COLUMN billing_for_supplies_only TO is_billing_for_supplies_only;

ALTER TABLE checklist_item RENAME COLUMN screensaver_user_id TO screening_room_user_id;

/* change cherry_pick_assay_plate descriminator */
ALTER TABLE cherry_pick_assay_plate ADD COLUMN cherry_pick_assay_plate_type varchar(31);
UPDATE cherry_pick_assay_plate SET cherry_pick_assay_plate_type = 'CherryPickAssayPlate' WHERE is_legacy = 'f';
UPDATE cherry_pick_assay_plate SET cherry_pick_assay_plate_type = 'LegacyCherryPickAssayPlate' WHERE is_legacy = 't';
ALTER TABLE cherry_pick_assay_plate ALTER COLUMN cherry_pick_assay_plate_type SET NOT NULL;
ALTER TABLE cherry_pick_assay_plate DROP COLUMN is_legacy;

ALTER TABLE cherry_pick_assay_plate ALTER COLUMN assay_plate_type SET NOT NULL;

ALTER TABLE cherry_pick_request DROP COLUMN ordinal;
ALTER TABLE cherry_pick_request RENAME COLUMN randomized_assay_plate_layout TO is_randomized_assay_plate_layout;

ALTER TABLE cherry_pick_request_requested_empty_columns RENAME TO cherry_pick_request_requested_empty_column;
ALTER TABLE cherry_pick_request_requested_empty_column RENAME COLUMN elt TO requested_empty_column;

/* the names of these constraints are reversed in the old schema */
ALTER TABLE collaborator_link DROP CONSTRAINT fk_collaborator_link_to_screen;
ALTER TABLE collaborator_link DROP CONSTRAINT fk_collaborator_link_to_screening_room_user;
ALTER TABLE collaborator_link ADD CONSTRAINT fk_collaborator_link_to_screen FOREIGN KEY (screen_id) REFERENCES screen;
ALTER TABLE collaborator_link ADD CONSTRAINT fk_collaborator_link_to_screening_room_user FOREIGN KEY (collaborator_id) REFERENCES screening_room_user;

ALTER TABLE compound ALTER COLUMN compound_id TYPE TEXT;
ALTER TABLE compound ALTER COLUMN inchi SET NOT NULL;

ALTER TABLE copy ALTER COLUMN copy_id TYPE TEXT;
ALTER TABLE copy ALTER COLUMN usage_type SET NOT NULL;

ALTER TABLE lab_affiliation ALTER COLUMN lab_affiliation_id TYPE TEXT;

ALTER TABLE result_value_type RENAME COLUMN derived TO is_derived;
ALTER TABLE result_value_type RENAME COLUMN follow_up_data TO is_follow_up_data;
ALTER TABLE result_value_type RENAME COLUMN numeric TO is_numeric;
ALTER TABLE result_value_type RENAME COLUMN positive_indicator TO is_positive_indicator;
ALTER TABLE result_value_type ALTER COLUMN name TYPE text;
ALTER TABLE result_value_type ALTER COLUMN ordinal SET NOT NULL;

ALTER TABLE result_value_type_result_values RENAME COLUMN result_value_type_id TO result_value_type;
ALTER TABLE result_value_type_result_values RENAME COLUMN key TO well_id;
/* warning: the following command on a production database requires GBs of free disk space: */
ALTER TABLE result_value_type_result_values ADD FOREIGN KEY (well_id) REFERENCES well(well_id);

ALTER TABLE result_value_type_result_values RENAME COLUMN exclude TO is_exclude;
ALTER TABLE result_value_type_result_values ALTER COLUMN is_exclude SET NOT NULL;
ALTER TABLE result_value_type_result_values RENAME COLUMN positive TO is_positive;
ALTER TABLE result_value_type_result_values ALTER COLUMN is_positive SET NOT NULL;
/* warning: untested against production database because i dont have enough disk space on my laptop to get this to run, and it also does not run on <v8 orchestra server: */
/* warning: the following command on a production database requires GBs of free disk space: */
ALTER TABLE result_value_type_result_values ALTER COLUMN value TYPE TEXT;

ALTER TABLE screen DROP COLUMN all_time_screening_room_activity_count;
ALTER TABLE screen DROP COLUMN all_time_cherry_pick_request_count;

ALTER TABLE screen_result_plate_numbers RENAME TO screen_result_plate_number;
ALTER TABLE screen_result_plate_number ADD PRIMARY KEY (screen_result_id, plate_number);

ALTER TABLE screen_result_well_link ALTER COLUMN well_id TYPE TEXT;

ALTER TABLE screener_cherry_pick ALTER COLUMN screened_well_id TYPE TEXT;
ALTER TABLE screener_cherry_pick ADD UNIQUE (cherry_pick_request_id, screened_well_id);

ALTER TABLE screening_room_activity DROP COLUMN ordinal;

ALTER TABLE screening_room_user DROP COLUMN lab_name;
ALTER TABLE screening_room_user RENAME COLUMN non_screening_user TO is_non_screening_user;

ALTER TABLE silencing_reagent ALTER COLUMN silencing_reagent_id TYPE TEXT;
ALTER TABLE silencing_reagent RENAME COLUMN pool_of_unknown_sequences TO is_pool_of_unknown_sequences;
ALTER TABLE silencing_reagent ADD UNIQUE (gene_id, silencing_reagent_type, sequence);

ALTER TABLE silencing_reagent_non_targetted_genbank_accession_number ALTER COLUMN silencing_reagent_id TYPE TEXT;

ALTER TABLE well ALTER COLUMN well_id TYPE TEXT;
ALTER TABLE well ADD UNIQUE (plate_number, well_name);

ALTER TABLE well_compound_link ALTER COLUMN compound_id TYPE TEXT;
ALTER TABLE well_compound_link ALTER COLUMN well_id TYPE TEXT;

ALTER TABLE well_molfile ALTER COLUMN well_id TYPE TEXT;
ALTER TABLE well_molfile ADD PRIMARY KEY (well_id);

ALTER TABLE well_silencing_reagent_link ALTER COLUMN well_id TYPE TEXT;
ALTER TABLE well_silencing_reagent_link ALTER COLUMN silencing_reagent_id TYPE TEXT;

/* the names of these constraints are reversed in the old schema */
ALTER TABLE well_silencing_reagent_link DROP CONSTRAINT fk_well_silencing_reagent_link_to_well;
ALTER TABLE well_silencing_reagent_link DROP CONSTRAINT fk_well_silencing_reagent_link_to_silencing_reagent;
ALTER TABLE well_silencing_reagent_link ADD CONSTRAINT fk_well_silencing_reagent_link_to_well FOREIGN KEY (well_id) REFERENCES well;
ALTER TABLE well_silencing_reagent_link ADD CONSTRAINT fk_well_silencing_reagent_link_to_silencing_reagent FOREIGN KEY (silencing_reagent_id) REFERENCES silencing_reagent;

ALTER TABLE well_volume_adjustment ALTER COLUMN microliter_volume SET NOT NULL;
ALTER TABLE well_volume_adjustment ALTER COLUMN copy_id TYPE TEXT;
ALTER TABLE well_volume_adjustment ALTER COLUMN well_id TYPE TEXT;

COMMIT;
