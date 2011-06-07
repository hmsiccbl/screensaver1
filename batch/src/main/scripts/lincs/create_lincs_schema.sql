
    create table abase_testset (
        abase_testset_id int4 not null,
        comments text not null,
        testset_date date not null,
        testset_name text not null,
        version int4 not null,
        screen_id int4 not null,
        primary key (abase_testset_id)
    );

    create table activity (
        activity_id int4 not null,
        date_created timestamp not null,
        comments text,
        date_of_activity date not null,
        version int4 not null,
        created_by_id int4,
        performed_by_id int4 not null,
        primary key (activity_id)
    );

    create table activity_update_activity (
        activity_id int4 not null,
        update_activity_id int4 not null,
        primary key (activity_id, update_activity_id)
    );

    create table administrative_activity (
        administrative_activity_type text not null,
        activity_id int4 not null,
        primary key (activity_id)
    );

    create table administrator_user (
        screensaver_user_id int4 not null,
        primary key (screensaver_user_id)
    );

    create table annotation_type (
        annotation_type_id int4 not null,
        description text,
        name text,
        is_numeric bool not null,
        ordinal int4 not null,
        version int4 not null,
        study_id int4 not null,
        primary key (annotation_type_id),
        unique (study_id, name)
    );

    create table annotation_value (
        annotation_value_id int4 not null,
        numeric_value float8,
        value text,
        annotation_type_id int4 not null,
        reagent_id int4 not null,
        primary key (annotation_value_id),
        unique (annotation_type_id, reagent_id)
    );

    create table assay_plate (
        assay_plate_id int4 not null,
        plate_number int4 not null,
        replicate_ordinal int4 not null,
        version int4 not null,
        library_screening_id int4,
        plate_id int4,
        screen_id int4 not null,
        screen_result_data_loading_id int4,
        primary key (assay_plate_id),
        unique (library_screening_id, plate_number, replicate_ordinal)
    );

    create table assay_well (
        assay_well_id int4 not null,
        assay_well_control_type text,
        confirmed_positive_value text,
        is_positive bool not null,
        version int4 not null,
        well_id text not null,
        screen_result_id int4 not null,
        primary key (assay_well_id),
        unique (screen_result_id, well_id)
    );

    create table attached_file (
        attached_file_id int4 not null,
        date_created timestamp not null,
        file_contents oid not null,
        file_date date,
        filename text not null,
        version int4 not null,
        created_by_id int4,
        attached_file_type_id int4 not null,
        reagent_id int4,
        screen_id int4,
        screensaver_user_id int4,
        primary key (attached_file_id),
        unique (screen_id, screensaver_user_id, filename)
    );

    create table attached_file_type (
        for_entity_type varchar(31) not null,
        attached_file_type_id int4 not null,
        value text not null unique,
        primary key (attached_file_type_id)
    );

    create table attached_file_update_activity (
        attached_file_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (attached_file_id, update_activity_id)
    );

    create table checklist_item (
        checklist_item_id int4 not null,
        checklist_item_group text not null,
        is_expirable bool not null,
        item_name text not null unique,
        order_statistic int4 not null,
        version int4 not null,
        primary key (checklist_item_id),
        unique (checklist_item_group, order_statistic)
    );

    create table checklist_item_event (
        checklist_item_event_id int4 not null,
        date_created timestamp not null,
        date_performed date,
        is_expiration bool not null,
        is_not_applicable bool not null,
        created_by_id int4,
        checklist_item_id int4 not null,
        screening_room_user_id int4 not null,
        primary key (checklist_item_event_id)
    );

    create table checklist_item_event_update_activity (
        checklist_item_event_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (checklist_item_event_id, update_activity_id)
    );

    create table cherry_pick_assay_plate (
        cherry_pick_assay_plate_type varchar(31) not null,
        cherry_pick_assay_plate_id int4 not null,
        assay_plate_type text not null,
        attempt_ordinal int4 not null,
        plate_ordinal int4 not null,
        version int4 not null,
        legacy_plate_name text,
        cherry_pick_liquid_transfer_id int4,
        cherry_pick_request_id int4 not null,
        primary key (cherry_pick_assay_plate_id),
        unique (cherry_pick_request_id, plate_ordinal, attempt_ordinal)
    );

    create table cherry_pick_assay_plate_screening_link (
        cherry_pick_assay_plate_id int4 not null,
        cherry_pick_screening_id int4 not null,
        primary key (cherry_pick_assay_plate_id, cherry_pick_screening_id)
    );

    create table cherry_pick_liquid_transfer (
        status text not null,
        activity_id int4 not null,
        primary key (activity_id)
    );

    create table cherry_pick_request (
        cherry_pick_request_id int4 not null,
        date_created timestamp not null,
        assay_plate_type text,
        assay_protocol_comments text,
        cherry_pick_assay_protocols_followed text,
        cherry_pick_followup_results_status text,
        comments text,
        date_requested date not null,
        date_volume_approved date,
        keep_source_plate_cherry_picks_together bool not null,
        legacy_cherry_pick_request_number int4,
        number_unfulfilled_lab_cherry_picks int4 not null,
        is_randomized_assay_plate_layout bool not null,
        transfer_volume_per_well_approved numeric(10, 9),
        transfer_volume_per_well_requested numeric(10, 9),
        version int4 not null,
        created_by_id int4,
        requested_by_id int4 not null,
        screen_id int4 not null,
        volume_approved_by_id int4,
        primary key (cherry_pick_request_id)
    );

    create table cherry_pick_request_empty_well (
        cherry_pick_request_id int4 not null,
        well_name varchar(3)
    );

    create table cherry_pick_request_update_activity (
        cherry_pick_request_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (cherry_pick_request_id, update_activity_id)
    );

    create table cherry_pick_screening (
        activity_id int4 not null,
        cherry_pick_request_id int4 not null,
        primary key (activity_id)
    );

    create table collaborator_link (
        screen_id int4 not null,
        collaborator_id int4 not null,
        primary key (screen_id, collaborator_id)
    );

    create table copy (
        copy_id int4 not null,
        date_created timestamp not null,
        comments varchar(255),
        date_plated date,
        name text not null,
        plate_locations_count int4,
        plates_available int4,
        primary_plate_mg_ml_concentration numeric(4, 1),
        primary_plate_molar_concentration numeric(12, 9),
        primary_plate_status text not null,
        usage_type text not null,
        version int4 not null,
        created_by_id int4,
        library_id int4 not null,
        primary_plate_location_id int4,
        primary key (copy_id),
        unique (library_id, name)
    );

    create table copy_update_activity (
        copy_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (copy_id, update_activity_id)
    );

    create table data_column (
        data_column_id int4 not null,
        assay_phenotype text,
        assay_readout_type text,
        cell_line text,
        channel int4,
        comments text,
        data_type text not null,
        decimal_places int4,
        is_derived bool not null,
        description text,
        is_follow_up_data bool not null,
        how_derived text,
        medium_positives_count int4,
        name text not null,
        ordinal int4 not null,
        positives_count int4,
        replicate_ordinal int4,
        strong_positives_count int4,
        time_point text,
        time_point_ordinal int4,
        version int4 not null,
        weak_positives_count int4,
        zdepth_ordinal int4,
        screen_result_id int4 not null,
        primary key (data_column_id)
    );

    create table data_column_derived_from_link (
        derived_from_data_column_id int4 not null,
        derived_data_column_id int4 not null,
        primary key (derived_from_data_column_id, derived_data_column_id)
    );

    create table equipment_used (
        equipment_used_id int4 not null,
        description text,
        equipment text not null,
        protocol text,
        version int4 not null,
        lab_activity_id int4 not null,
        primary key (equipment_used_id)
    );

    create table funding_support (
        funding_support_id int4 not null,
        value text unique,
        primary key (funding_support_id)
    );

    create table gene (
        gene_id int4 not null,
        entrezgene_id int4,
        gene_name text,
        species_name text,
        primary key (gene_id)
    );

    create table gene_genbank_accession_number (
        gene_id int4 not null,
        genbank_accession_number text not null,
        primary key (gene_id, genbank_accession_number)
    );

    create table gene_symbol (
        gene_id int4 not null,
        entrezgene_symbol text not null,
        primary key (gene_id, entrezgene_symbol)
    );

    create table lab_activity (
        molar_concentration numeric(12, 9),
        volume_transferred_per_well numeric(10, 9),
        activity_id int4 not null,
        screen_id int4 not null,
        primary key (activity_id)
    );

    create table lab_affiliation (
        lab_affiliation_id int4 not null,
        affiliation_category text not null,
        affiliation_name text not null unique,
        version int4 not null,
        primary key (lab_affiliation_id)
    );

    create table lab_cherry_pick (
        lab_cherry_pick_id int4 not null,
        assay_plate_column int4,
        assay_plate_row int4,
        version int4 not null,
        cherry_pick_assay_plate_id int4,
        cherry_pick_request_id int4 not null,
        screener_cherry_pick_id int4 not null,
        source_well_id text not null,
        primary key (lab_cherry_pick_id)
    );

    create table lab_head (
        screensaver_user_id int4 not null,
        lab_affiliation_id int4,
        primary key (screensaver_user_id)
    );

    create table library (
        library_id int4 not null,
        date_created timestamp not null,
        date_received date,
        date_screenable date,
        description text,
        end_plate int4 not null unique,
        experimental_well_count int4,
        library_name text not null unique,
        library_type text not null,
        plate_size text not null,
        is_pool bool not null,
        provider text,
        quality_control_information text,
        screen_type text not null,
        screening_status text not null,
        short_name text not null unique,
        solvent text not null,
        start_plate int4 not null unique,
        version int4 not null,
        created_by_id int4,
        latest_released_contents_version_id int4,
        owner_screener_id int4,
        primary key (library_id)
    );

    create table library_contents_version (
        library_contents_version_id int4 not null,
        version int4 not null,
        version_number int4 not null,
        library_id int4 not null,
        library_contents_loading_activity_id int4 not null,
        library_contents_release_activity_id int4,
        primary key (library_contents_version_id),
        unique (library_id, version_number)
    );

    create table library_screening (
        abase_testset_id text,
        is_for_external_library_plates bool not null,
        libraries_screened_count int4 not null,
        library_plates_screened_count int4 not null,
        screened_experimental_well_count int4 not null,
        activity_id int4 not null,
        primary key (activity_id)
    );

    create table library_update_activity (
        library_id int4 not null,
        update_activity_id int4 not null,
        primary key (library_id, update_activity_id)
    );

    create table molfile (
        reagent_id int4 not null unique,
        molfile text not null,
        ordinal int4 not null,
        primary key (reagent_id, ordinal)
    );

    create table natural_product_reagent (
        reagent_id int4 not null,
        primary key (reagent_id)
    );

    create table plate (
        plate_id int4 not null,
        date_created timestamp not null,
        facility_id varchar(255),
        mg_ml_concentration numeric(4, 1),
        molar_concentration numeric(12, 9),
        plate_number int4 not null,
        plate_type text,
        status text not null,
        stock_plate_number int4,
        quadrant int4,
        version int4 not null,
        well_volume numeric(10, 9),
        created_by_id int4,
        copy_id int4 not null,
        plate_location_id int4,
        plated_activity_id int4 unique,
        retired_activity_id int4 unique,
        primary key (plate_id),
        unique (copy_id, plate_number)
    );

    create table plate_location (
        plate_location_id int4 not null,
        bin text not null,
        freezer text not null,
        room text not null,
        shelf text not null,
        primary key (plate_location_id),
        unique (room, freezer, shelf, bin)
    );

    create table plate_update_activity (
        plate_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (plate_id, update_activity_id)
    );

    create table publication (
        publication_id int4 not null,
        authors text,
        journal text,
        pages text,
        pubmed_central_id int4,
        pubmed_id int4,
        title text,
        version int4 not null,
        volume text,
        year_published text,
        attached_file_id int4 unique,
        primary key (publication_id)
    );

    create table reagent (
        reagent_id int4 not null,
        vendor_identifier text,
        vendor_name text,
        library_contents_version_id int4 not null,
        well_id text not null,
        primary key (reagent_id),
        unique (well_id, library_contents_version_id)
    );

    create table reagent_publication_link (
        reagent_id int4 not null,
        publication_id int4 not null,
        primary key (reagent_id, publication_id)
    );

    create table result_value (
        result_value_id int4 not null,
        assay_well_control_type text,
        value text,
        is_exclude bool not null,
        numeric_value float8,
        is_positive bool not null,
        data_column_id int4 not null,
        well_id text not null,
        primary key (result_value_id),
        unique (data_column_id, well_id)
    );

    create table rnai_cherry_pick_request (
        cherry_pick_request_id int4 not null,
        primary key (cherry_pick_request_id)
    );

    create table screen (
        screen_id int4 not null,
        date_created timestamp not null,
        abase_protocol_id text,
        abase_study_id text,
        assay_plates_screened_count int4 not null,
        amount_to_be_charged_for_screen numeric(9, 2),
        billing_comments text,
        is_billing_for_supplies_only bool not null,
        billing_info_return_date date,
        date_charged date,
        date_completed5kcompounds date,
        date_faxed_to_billing_department date,
        facilities_and_administration_charge numeric(9, 2),
        is_fee_form_on_file bool not null,
        fee_form_requested_date date,
        fee_form_requested_initials text,
        see_comments bool not null,
        to_be_requested bool not null,
        comments text,
        coms_approval_date date,
        coms_registration_number text,
        data_meeting_complete date,
        data_meeting_scheduled date,
        data_privacy_expiration_date date,
        data_privacy_expiration_notified_date date,
        data_sharing_level int4 not null,
        date_of_application date,
        facility_id text not null unique,
        libraries_screened_count int4 not null,
        library_plates_data_analyzed_count int4 not null,
        library_plates_data_loaded_count int4 not null,
        library_plates_screened_count int4 not null,
        max_allowed_data_privacy_expiration_date date,
        max_data_loaded_replicate_count int4,
        max_screened_replicate_count int4,
        min_allowed_data_privacy_expiration_date date,
        min_data_loaded_replicate_count int4,
        min_screened_replicate_count int4,
        project_id text,
        project_phase text not null,
        pubchem_assay_id int4,
        pubchem_deposited_date date,
        publishable_protocol text,
        publishable_protocol_comments text,
        publishable_protocol_date_entered date,
        publishable_protocol_entered_by text,
        screen_type text not null,
        screened_experimental_well_count int4 not null,
        study_type text not null,
        summary text,
        title text not null,
        total_plated_lab_cherry_picks int4 not null,
        unique_screened_experimental_well_count int4 not null,
        url text,
        version int4 not null,
        created_by_id int4,
        lab_head_id int4,
        lead_screener_id int4,
        pin_transfer_admin_activity_id int4,
        primary key (screen_id)
    );

    create table screen_billing_item (
        screen_id int4 not null,
        amount numeric(9, 2) not null,
        date_sent_for_billing date,
        item_to_be_charged text not null,
        ordinal int4 not null,
        primary key (screen_id, ordinal)
    );

    create table screen_funding_support_link (
        screen_id int4 not null,
        funding_support_id int4 not null,
        primary key (screen_id, funding_support_id)
    );

    create table screen_keyword (
        screen_id int4 not null,
        keyword text not null,
        primary key (screen_id, keyword)
    );

    create table screen_publication_link (
        screen_id int4 not null,
        publication_id int4 not null,
        primary key (screen_id, publication_id)
    );

    create table screen_result (
        screen_result_id int4 not null,
        date_created timestamp not null,
        channel_count int4 not null,
        experimental_well_count int4 not null,
        replicate_count int4 not null,
        version int4 not null,
        created_by_id int4,
        screen_id int4 not null unique,
        primary key (screen_result_id)
    );

    create table screen_result_update_activity (
        screen_result_id int4 not null,
        update_activity_id int4 not null,
        primary key (screen_result_id, update_activity_id)
    );

    create table screen_status_item (
        screen_id int4 not null,
        status text not null,
        status_date date not null,
        primary key (screen_id, status, status_date)
    );

    create table screen_update_activity (
        screen_id int4 not null,
        update_activity_id int4 not null unique,
        primary key (screen_id, update_activity_id)
    );

    create table screener_cherry_pick (
        screener_cherry_pick_id int4 not null,
        version int4 not null,
        cherry_pick_request_id int4 not null,
        screened_well_id text not null,
        primary key (screener_cherry_pick_id),
        unique (cherry_pick_request_id, screened_well_id)
    );

    create table screening (
        assay_protocol text,
        assay_protocol_last_modified_date date,
        assay_protocol_type text,
        assay_well_volume numeric(10, 9),
        number_of_replicates int4,
        activity_id int4 not null,
        primary key (activity_id)
    );

    create table screening_room_user (
        coms_crhba_permit_number text,
        coms_crhba_permit_principal_investigator text,
        user_classification text not null,
        screensaver_user_id int4 not null,
        lab_head_id int4,
        last_notified_smua_checklist_item_event_id int4,
        primary key (screensaver_user_id)
    );

    create table screening_room_user_facility_usage_role (
        screening_room_user_id int4 not null,
        facility_usage_role text not null,
        primary key (screening_room_user_id, facility_usage_role)
    );

    create table screensaver_user (
        screensaver_user_id int4 not null,
        date_created timestamp not null,
        ecommons_id text,
        comments text,
        digested_password text,
        email text,
        first_name text not null,
        harvard_id text,
        harvard_id_expiration_date date,
        harvard_id_requested_expiration_date date,
        last_name text not null,
        login_id text unique,
        mailing_address text,
        phone text,
        version int4 not null,
        created_by_id int4,
        primary key (screensaver_user_id)
    );

    create table screensaver_user_role (
        screensaver_user_id int4 not null,
        screensaver_user_role text not null,
        primary key (screensaver_user_id, screensaver_user_role)
    );

    create table screensaver_user_update_activity (
        screensaver_user_id int4 not null,
        update_activity_id int4 not null,
        primary key (screensaver_user_id, update_activity_id)
    );

    create table silencing_reagent (
        sequence text,
        silencing_reagent_type text,
        reagent_id int4 not null,
        facility_gene_id int4 unique,
        vendor_gene_id int4 unique,
        primary key (reagent_id)
    );

    create table silencing_reagent_duplex_wells (
        silencing_reagent_id int4 not null,
        well_id text not null,
        primary key (silencing_reagent_id, well_id)
    );

    create table small_molecule_chembank_id (
        reagent_id int4 not null,
        chembank_id int4 not null,
        primary key (reagent_id, chembank_id)
    );

    create table small_molecule_chembl_id (
        reagent_id int4 not null,
        chembl_id int4 not null,
        primary key (reagent_id, chembl_id)
    );

    create table small_molecule_cherry_pick_request (
        cherry_pick_request_id int4 not null,
        primary key (cherry_pick_request_id)
    );

    create table small_molecule_compound_name (
        reagent_id int4 not null,
        compound_name text not null,
        ordinal int4 not null,
        primary key (reagent_id, ordinal)
    );

    create table small_molecule_pubchem_cid (
        reagent_id int4 not null,
        pubchem_cid int4 not null,
        primary key (reagent_id, pubchem_cid)
    );

    create table small_molecule_reagent (
        facility_batch_id int4,
        inchi text,
        molecular_formula text,
        molecular_mass numeric(15, 9),
        molecular_weight numeric(15, 9),
        salt_form_id int4,
        smiles text,
        vendor_batch_id text,
        reagent_id int4 not null,
        primary key (reagent_id)
    );

    create table study_reagent_link (
        study_id int4 not null,
        reagent_id int4 not null,
        primary key (study_id, reagent_id)
    );

    create table well (
        well_id text not null,
        is_deprecated bool not null,
        facility_id text,
        library_well_type text not null,
        molar_concentration numeric(15, 12),
        plate_number int4 not null,
        version int4 not null,
        well_name text not null,
        deprecation_admin_activity_id int4,
        latest_released_reagent_id int4,
        library_id int4 not null,
        primary key (well_id),
        unique (plate_number, well_name)
    );

    create table well_volume_adjustment (
        well_volume_adjustment_id int4 not null,
        version int4 not null,
        volume numeric(10, 9) not null,
        copy_id int4 not null,
        lab_cherry_pick_id int4,
        well_id text not null,
        well_volume_correction_activity_id int4,
        primary key (well_volume_adjustment_id),
        unique (copy_id, well_id, lab_cherry_pick_id, well_volume_correction_activity_id)
    );

    create table well_volume_correction_activity (
        activity_id int4 not null,
        primary key (activity_id)
    );

    alter table abase_testset 
        add constraint fk_abase_testset_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table activity 
        add constraint fk_activity_to_screensaver_user 
        foreign key (performed_by_id) 
        references screensaver_user;

    alter table activity 
        add constraint FK9D4BF30F66AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table activity_update_activity 
        add constraint FKC7350895ACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table activity_update_activity 
        add constraint FKC7350895702E36D6 
        foreign key (activity_id) 
        references activity;

    alter table administrative_activity 
        add constraint fk_administrative_activity_to_activity 
        foreign key (activity_id) 
        references activity;

    alter table administrator_user 
        add constraint fk_administrator_user_to_screensaver_user 
        foreign key (screensaver_user_id) 
        references screensaver_user;

    alter table annotation_type 
        add constraint fk_annotation_type_to_screen 
        foreign key (study_id) 
        references screen;

    create index annot_value_annot_type_and_value_index on annotation_value (annotation_type_id, value);

    create index annot_value_annot_type_and_numeric_value_index on annotation_value (annotation_type_id, numeric_value);

    alter table annotation_value 
        add constraint fk_annotation_value_to_reagent 
        foreign key (reagent_id) 
        references reagent;

    alter table annotation_value 
        add constraint fk_annotation_value_to_annotation_type 
        foreign key (annotation_type_id) 
        references annotation_type;

    alter table assay_plate 
        add constraint fk_assay_plate_to_screen_result_data_loading 
        foreign key (screen_result_data_loading_id) 
        references administrative_activity;

    alter table assay_plate 
        add constraint fk_assay_plate_to_plate 
        foreign key (plate_id) 
        references plate;

    alter table assay_plate 
        add constraint fk_assay_plate_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table assay_plate 
        add constraint fk_assay_plate_to_library_screening 
        foreign key (library_screening_id) 
        references library_screening;

    create index assay_well_confirmed_positives_data_only_index on assay_well (confirmed_positive_value);

    create index assay_well_well_positives_only_index on assay_well (is_positive);

    alter table assay_well 
        add constraint fk_assay_well_to_well 
        foreign key (well_id) 
        references well;

    alter table assay_well 
        add constraint fk_assay_well_to_screen_result 
        foreign key (screen_result_id) 
        references screen_result;

    alter table attached_file 
        add constraint fk_attached_file_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table attached_file 
        add constraint fk_attached_file_to_screening_room_user 
        foreign key (screensaver_user_id) 
        references screening_room_user;

    alter table attached_file 
        add constraint fk_attached_file_to_reagent 
        foreign key (reagent_id) 
        references reagent;

    alter table attached_file 
        add constraint fk_attached_file_to_attached_file_type 
        foreign key (attached_file_type_id) 
        references attached_file_type;

    alter table attached_file 
        add constraint FKFC6F173766AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table attached_file_update_activity 
        add constraint FKFE1ADCBDACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table attached_file_update_activity 
        add constraint FKFE1ADCBDD73F25DF 
        foreign key (attached_file_id) 
        references attached_file;

    alter table checklist_item_event 
        add constraint fk_checklist_item_event_to_screening_room_user 
        foreign key (screening_room_user_id) 
        references screening_room_user;

    alter table checklist_item_event 
        add constraint FK21A896A766AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table checklist_item_event 
        add constraint fk_checklist_item_event_to_checklist_item 
        foreign key (checklist_item_id) 
        references checklist_item;

    alter table checklist_item_event_update_activity 
        add constraint FK20C77C2DACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table checklist_item_event_update_activity 
        add constraint FK20C77C2D84DCB316 
        foreign key (checklist_item_event_id) 
        references checklist_item_event;

    alter table cherry_pick_assay_plate 
        add constraint fk_cherry_pick_assay_plate_to_cherry_pick_liquid_transfer 
        foreign key (cherry_pick_liquid_transfer_id) 
        references cherry_pick_liquid_transfer;

    alter table cherry_pick_assay_plate 
        add constraint fk_cherry_pick_assay_plate_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table cherry_pick_assay_plate_screening_link 
        add constraint FKFB859D2AF11EC30A 
        foreign key (cherry_pick_assay_plate_id) 
        references cherry_pick_assay_plate;

    alter table cherry_pick_assay_plate_screening_link 
        add constraint FKFB859D2A8458B415 
        foreign key (cherry_pick_screening_id) 
        references cherry_pick_screening;

    alter table cherry_pick_liquid_transfer 
        add constraint fk_cherry_pick_liquid_transfer_to_activity 
        foreign key (activity_id) 
        references lab_activity;

    alter table cherry_pick_request 
        add constraint fk_cherry_pick_request_to_administrator_user 
        foreign key (volume_approved_by_id) 
        references administrator_user;

    alter table cherry_pick_request 
        add constraint fk_cherry_pick_request_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table cherry_pick_request 
        add constraint fk_cherry_pick_request_to_screening_room_user 
        foreign key (requested_by_id) 
        references screening_room_user;

    alter table cherry_pick_request 
        add constraint FKA0AF7D5766AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table cherry_pick_request_empty_well 
        add constraint fk_cherry_pick_request_empty_wells_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table cherry_pick_request_update_activity 
        add constraint FKE60B02DDACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table cherry_pick_request_update_activity 
        add constraint FKE60B02DDCEAC5AA7 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table cherry_pick_screening 
        add constraint fk_cherry_pick_screening_to_activity 
        foreign key (activity_id) 
        references screening;

    alter table cherry_pick_screening 
        add constraint fk_cherry_pick_screening_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table collaborator_link 
        add constraint fk_collaborator_link_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table collaborator_link 
        add constraint fk_collaborator_link_to_screening_room_user 
        foreign key (collaborator_id) 
        references screening_room_user;

    alter table copy 
        add constraint fk_copy_to_library 
        foreign key (library_id) 
        references library;

    alter table copy 
        add constraint FK2EAF7566AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table copy 
        add constraint fk_copy_to_primary_plate_location 
        foreign key (primary_plate_location_id) 
        references plate_location;

    alter table copy_update_activity 
        add constraint FK8CE38FB32836E4B 
        foreign key (copy_id) 
        references copy;

    alter table copy_update_activity 
        add constraint FK8CE38FBACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table data_column 
        add constraint fk_data_column_to_screen_result 
        foreign key (screen_result_id) 
        references screen_result;

    alter table data_column_derived_from_link 
        add constraint fk_derived_from_data_column 
        foreign key (derived_from_data_column_id) 
        references data_column;

    alter table data_column_derived_from_link 
        add constraint fk_derived_data_column 
        foreign key (derived_data_column_id) 
        references data_column;

    alter table equipment_used 
        add constraint fk_equipment_used_to_lab_activity 
        foreign key (lab_activity_id) 
        references lab_activity;

    alter table gene_genbank_accession_number 
        add constraint fk_gene_genbank_accession_number_to_gene 
        foreign key (gene_id) 
        references gene;

    alter table gene_symbol 
        add constraint fk_gene_symbol_to_gene 
        foreign key (gene_id) 
        references gene;

    alter table lab_activity 
        add constraint fk_lab_activity_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table lab_activity 
        add constraint fk_lab_activity_to_activity 
        foreign key (activity_id) 
        references activity;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_cherry_pick_assay_plate 
        foreign key (cherry_pick_assay_plate_id) 
        references cherry_pick_assay_plate;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_screener_cherry_pick 
        foreign key (screener_cherry_pick_id) 
        references screener_cherry_pick;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table lab_cherry_pick 
        add constraint fk_lab_cherry_pick_to_source_well 
        foreign key (source_well_id) 
        references well;

    alter table lab_head 
        add constraint fk_lab_head_to_lab_affiliation 
        foreign key (lab_affiliation_id) 
        references lab_affiliation;

    alter table lab_head 
        add constraint fk_lab_head_to_screening_room_user 
        foreign key (screensaver_user_id) 
        references screening_room_user;

    alter table library 
        add constraint fk_library_to_owner 
        foreign key (owner_screener_id) 
        references screening_room_user;

    alter table library 
        add constraint FK9E824BBFFD9E071 
        foreign key (latest_released_contents_version_id) 
        references library_contents_version;

    alter table library 
        add constraint FK9E824BB66AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table library_contents_version 
        add constraint FK4F9A4FB7426C6700 
        foreign key (library_contents_loading_activity_id) 
        references administrative_activity;

    alter table library_contents_version 
        add constraint fk_library_contents_version_to_library 
        foreign key (library_id) 
        references library;

    alter table library_contents_version 
        add constraint FK4F9A4FB77D09D54B 
        foreign key (library_contents_release_activity_id) 
        references administrative_activity;

    alter table library_screening 
        add constraint fk_library_screening_to_activity 
        foreign key (activity_id) 
        references screening;

    alter table library_update_activity 
        add constraint FK31706241ACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table library_update_activity 
        add constraint FK317062411E25BD89 
        foreign key (library_id) 
        references library;

    alter table molfile 
        add constraint FK499307862F5B5BE 
        foreign key (reagent_id) 
        references small_molecule_reagent;

    alter table natural_product_reagent 
        add constraint FKC0F2D4C161EA629 
        foreign key (reagent_id) 
        references reagent;

    alter table plate 
        add constraint FK65CDB166F3F8673 
        foreign key (plated_activity_id) 
        references administrative_activity;

    alter table plate 
        add constraint FK65CDB167101618E 
        foreign key (retired_activity_id) 
        references administrative_activity;

    alter table plate 
        add constraint fk_plate_to_copy 
        foreign key (copy_id) 
        references copy;

    alter table plate 
        add constraint fk_plate_to_plate_location 
        foreign key (plate_location_id) 
        references plate_location;

    alter table plate 
        add constraint FK65CDB1666AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table plate_update_activity 
        add constraint FK361B029CACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table plate_update_activity 
        add constraint FK361B029CAC7FFB69 
        foreign key (plate_id) 
        references plate;

    alter table publication 
        add constraint fk_publication_to_attached_file 
        foreign key (attached_file_id) 
        references attached_file;

    create index reagent_vendor_identifier_index on reagent (vendor_identifier);

    alter table reagent 
        add constraint fk_reagent_to_library_contents_version 
        foreign key (library_contents_version_id) 
        references library_contents_version;

    alter table reagent 
        add constraint fk_reagent_to_well 
        foreign key (well_id) 
        references well;

    alter table reagent_publication_link 
        add constraint FKEA69421A95666B57 
        foreign key (publication_id) 
        references publication;

    alter table reagent_publication_link 
        add constraint fk_reagent_publication_link_to_reagent 
        foreign key (reagent_id) 
        references reagent;

    create index result_value_data_column_and_numeric_value_index on result_value (data_column_id, numeric_value);

    create index result_value_data_column_and_value_index on result_value (data_column_id, value);

    alter table result_value 
        add constraint fk_result_value_to_data_column 
        foreign key (data_column_id) 
        references data_column;

    alter table result_value 
        add constraint fk_result_value_to_well 
        foreign key (well_id) 
        references well;

    alter table rnai_cherry_pick_request 
        add constraint fk_rnai_cherry_pick_request_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table screen 
        add constraint fk_screen_to_lab_head 
        foreign key (lab_head_id) 
        references lab_head;

    alter table screen 
        add constraint fk_screen_to_lead_screener 
        foreign key (lead_screener_id) 
        references screening_room_user;

    alter table screen 
        add constraint fk_screen_to_pin_transfer_admin_activity 
        foreign key (pin_transfer_admin_activity_id) 
        references administrative_activity;

    alter table screen 
        add constraint FKC9E5C06C66AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table screen_billing_item 
        add constraint FKC41F4A806C52FD 
        foreign key (screen_id) 
        references screen;

    alter table screen_funding_support_link 
        add constraint FKEAA8B25F1FCB31E2 
        foreign key (funding_support_id) 
        references funding_support;

    alter table screen_funding_support_link 
        add constraint FKEAA8B25F806C52FD 
        foreign key (screen_id) 
        references screen;

    alter table screen_keyword 
        add constraint fk_screen_keyword_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table screen_publication_link 
        add constraint FK81A349A095666B57 
        foreign key (publication_id) 
        references publication;

    alter table screen_publication_link 
        add constraint fk_screen_publication_link_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table screen_result 
        add constraint fk_screen_result_to_screen 
        foreign key (screen_id) 
        references screen;

    alter table screen_result 
        add constraint FK58DEF35066AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table screen_result_update_activity 
        add constraint FK77E966D6ACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table screen_result_update_activity 
        add constraint FK77E966D6852975F3 
        foreign key (screen_result_id) 
        references screen_result;

    alter table screen_status_item 
        add constraint FKA45567ED806C52FD 
        foreign key (screen_id) 
        references screen;

    alter table screen_update_activity 
        add constraint FKF64A7BF2ACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table screen_update_activity 
        add constraint FKF64A7BF2806C52FD 
        foreign key (screen_id) 
        references screen;

    alter table screener_cherry_pick 
        add constraint fk_screener_cherry_pick_to_screened_well 
        foreign key (screened_well_id) 
        references well;

    alter table screener_cherry_pick 
        add constraint fk_screener_cherry_pick_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table screening 
        add constraint fk_screening_to_activity 
        foreign key (activity_id) 
        references lab_activity;

    alter table screening_room_user 
        add constraint fk_screening_room_user_to_lab_head 
        foreign key (lab_head_id) 
        references lab_head;

    alter table screening_room_user 
        add constraint fk_screening_room_user_to_screensaver_user 
        foreign key (screensaver_user_id) 
        references screensaver_user;

    alter table screening_room_user 
        add constraint fk_screening_room_user_to_notified_checklist_item_event 
        foreign key (last_notified_smua_checklist_item_event_id) 
        references checklist_item_event;

    alter table screening_room_user_facility_usage_role 
        add constraint FK14622D574A89084E 
        foreign key (screening_room_user_id) 
        references screening_room_user;

    alter table screensaver_user 
        add constraint FK39B734A166AB751E 
        foreign key (created_by_id) 
        references screensaver_user;

    alter table screensaver_user_role 
        add constraint fk_screensaver_user_role_type_to_screensaver_user 
        foreign key (screensaver_user_id) 
        references screensaver_user;

    alter table screensaver_user_update_activity 
        add constraint FK4FDEE627ACAEF6AE 
        foreign key (update_activity_id) 
        references administrative_activity;

    alter table screensaver_user_update_activity 
        add constraint FK4FDEE627612ADCAB 
        foreign key (screensaver_user_id) 
        references screensaver_user;

    alter table silencing_reagent 
        add constraint FKBA0F32912160CE54 
        foreign key (vendor_gene_id) 
        references gene;

    alter table silencing_reagent 
        add constraint FKBA0F3291B09B0CAF 
        foreign key (facility_gene_id) 
        references gene;

    alter table silencing_reagent 
        add constraint FKBA0F3291161EA629 
        foreign key (reagent_id) 
        references reagent;

    alter table silencing_reagent_duplex_wells 
        add constraint FK4769F14433A43AB 
        foreign key (well_id) 
        references well;

    alter table silencing_reagent_duplex_wells 
        add constraint FK4769F14DB917E6E 
        foreign key (silencing_reagent_id) 
        references silencing_reagent;

    alter table small_molecule_chembank_id 
        add constraint fk_small_molecule_chembank_id_to_small_molecule_reagent 
        foreign key (reagent_id) 
        references small_molecule_reagent;

    alter table small_molecule_chembl_id 
        add constraint fk_small_molecule_chembl_id_to_small_molecule_reagent 
        foreign key (reagent_id) 
        references small_molecule_reagent;

    alter table small_molecule_cherry_pick_request 
        add constraint fk_small_molecule_cherry_pick_request_to_cherry_pick_request 
        foreign key (cherry_pick_request_id) 
        references cherry_pick_request;

    alter table small_molecule_compound_name 
        add constraint fk_small_molecule_compound_name_id_to_small_molecule_reagent 
        foreign key (reagent_id) 
        references small_molecule_reagent;

    alter table small_molecule_pubchem_cid 
        add constraint fk_small_molecule_pubchem_id_to_small_molecule_reagent 
        foreign key (reagent_id) 
        references small_molecule_reagent;

    alter table small_molecule_reagent 
        add constraint FKF5A7B431161EA629 
        foreign key (reagent_id) 
        references reagent;

    alter table study_reagent_link 
        add constraint fk_reagent_link_to_study 
        foreign key (study_id) 
        references screen;

    alter table study_reagent_link 
        add constraint fk_reagent_to_study 
        foreign key (reagent_id) 
        references reagent;

    alter table well 
        add constraint FK37A0CE8B2EFDB3 
        foreign key (latest_released_reagent_id) 
        references reagent;

    alter table well 
        add constraint fk_well_to_deprecation_admin_activity 
        foreign key (deprecation_admin_activity_id) 
        references administrative_activity;

    alter table well 
        add constraint fk_well_to_library 
        foreign key (library_id) 
        references library;

    alter table well_volume_adjustment 
        add constraint fk_well_volume_adjustment_to_copy 
        foreign key (copy_id) 
        references copy;

    alter table well_volume_adjustment 
        add constraint fk_well_volume_adjustment_to_lab_cherry_pick 
        foreign key (lab_cherry_pick_id) 
        references lab_cherry_pick;

    alter table well_volume_adjustment 
        add constraint fk_well_volume_adjustment_to_well 
        foreign key (well_id) 
        references well;

    alter table well_volume_adjustment 
        add constraint fk_well_volume_adjustment_to_well_volume_correction_activity 
        foreign key (well_volume_correction_activity_id) 
        references well_volume_correction_activity;

    alter table well_volume_correction_activity 
        add constraint fk_well_volume_correction_activity_to_activity 
        foreign key (activity_id) 
        references administrative_activity;

    create sequence abase_testset_id_seq;

    create sequence activity_id_seq;

    create sequence annotation_type_id_seq;

    create sequence annotation_value_id_seq;

    create sequence assay_plate_id_seq;

    create sequence assay_well_id_seq;

    create sequence attached_file_id_seq;

    create sequence attached_file_type_id_seq;

    create sequence checklist_item_event_id_seq;

    create sequence checklist_item_id_seq;

    create sequence cherry_pick_assay_plate_id_seq;

    create sequence cherry_pick_request_id_seq start with 10000;

    create sequence copy_id_seq;

    create sequence data_column_id_seq;

    create sequence equipment_used_id_seq;

    create sequence funding_support_id_seq;

    create sequence gene_id_seq;

    create sequence lab_affiliation_id_seq;

    create sequence lab_cherry_pick_id_seq;

    create sequence library_contents_version_id_seq;

    create sequence library_id_seq;

    create sequence plate_id_seq;

    create sequence plate_location_id_seq;

    create sequence publication_id_seq;

    create sequence reagent_id_seq;

    create sequence result_value_id_seq;

    create sequence screen_id_seq;

    create sequence screen_result_id_seq;

    create sequence screener_cherry_pick_id_seq;

    create sequence screensaver_user_id_seq;

    create sequence well_volume_adjustment_id_seq;
