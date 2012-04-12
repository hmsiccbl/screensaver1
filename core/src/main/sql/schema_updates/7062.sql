/** NOTE: this script updates the database for all trunk builds, but features using 
*   these changes have been enabled on the LINCS build only, for now.  see: [#3200]
**/
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7062,
current_timestamp,
'new entities for LINCS: ExperimentalCellInformation, Cell';

/* this column was not being used */
alter table data_column drop column cell_line; 

/* create new tables */

    create table cell (
        cell_id int4 not null,
        alternate_id varchar(255),
        alternate_name varchar(255),
        batch_id varchar(255),
        cell_type varchar(255),
        cell_type_detail text,
        center_name varchar(255),
        center_specific_id varchar(255),
        clo_id varchar(255),
        disease varchar(255),
        disease_detail text,
        facility_id varchar(255) not null unique,
        genetic_modification varchar(255),
        mutations_explicit text,
        mutations_reference text,
        name varchar(255),
        organ varchar(255),
        organism varchar(255),
        organism_gender varchar(255),
        recommended_culture_conditions text,
        tissue varchar(255),
        vendor varchar(255),
        vendor_catalog_id varchar(255),
        verification text,
        verification_reference_profile text,
        primary key (cell_id)
    );

    create table cell_growth_properties (
        cell_id int4 not null,
        growth_property text not null,
        primary key (cell_id, growth_property)
    );

    create table cell_lineage (
        cell_id int4 not null,
        primary key (cell_id)
    );

    create table cell_markers (
        cell_id int4 not null,
        cell_markers text not null,
        primary key (cell_id, cell_markers)
    );

    create table cell_related_projects (
        cell_id int4 not null,
        related_project text not null,
        primary key (cell_id, related_project)
    );
    
    create table experimental_cell_information (
        experimental_cell_information_id int4 not null,
        cell_id int4 not null,
        screen_id int4 not null,
        primary key (experimental_cell_information_id)
    );
    
    create table primary_cell (
        age_in_years int4 not null,
        donor_ethnicity varchar(255),
        donor_health_status varchar(255),
        passage_number int4 not null,
        cell_id int4 not null,
        primary key (cell_id)
    );
    
    alter table cell_growth_properties 
        add constraint fk_cell_growth_properties_to_cell 
        foreign key (cell_id) 
        references cell;

    alter table cell_lineage 
        add constraint fk_cell_lineage_to_cell 
        foreign key (cell_id) 
        references cell;

    alter table cell_markers 
        add constraint fk_cell_markers_to_cell 
        foreign key (cell_id) 
        references primary_cell;

    alter table cell_related_projects 
        add constraint fk_cell_related_projects_to_cell 
        foreign key (cell_id) 
        references cell;

    alter table experimental_cell_information 
        add constraint fk_experimental_cell_information_link_to_cell 
        foreign key (cell_id) 
        references cell;

    alter table experimental_cell_information 
        add constraint fk_experimental_cell_information_set_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table primary_cell 
        add constraint fk_primary_cell_to_cell 
        foreign key (cell_id) 
        references cell;

    create sequence cell_id_seq;

    create sequence exp_cell_information_id_seq;

COMMIT;