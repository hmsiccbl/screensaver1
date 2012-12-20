BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7241,
current_timestamp,
'Convert silencing reagent vendor and facility gene to an ordered list';

    create table reagent_facility_genes (
        reagent_id int4 not null,
        gene_id int4 not null,
        ordinal int4 not null,
        primary key (reagent_id, ordinal),
        unique (gene_id)
    );


    create table reagent_vendor_genes (
        reagent_id int4 not null,
        gene_id int4 not null,
        ordinal int4 not null,
        primary key (reagent_id, ordinal),
        unique (gene_id)
    );

    alter table reagent_facility_genes                                                                                                                                                                                                                                                                                                
        add constraint fk_facility_genes_to_reagent 
        foreign key (reagent_id) 
        references silencing_reagent;

    alter table reagent_facility_genes
        add constraint FK2C392A8FF4E84A4B
        foreign key (gene_id)
        references gene;

    alter table reagent_vendor_genes
        add constraint fk_vendor_genes_to_reagent
        foreign key (reagent_id)
        references silencing_reagent;

    alter table reagent_vendor_genes
        add constraint FK1D62F8B4F4E84A4B
        foreign key (gene_id)
        references gene;


    insert into reagent_facility_genes ( reagent_id, gene_id, ordinal ) 
    select reagent_id, facility_gene_id, 0 AS ordinal
    from silencing_reagent
    where facility_gene_id is not null;
    
    insert into reagent_vendor_genes ( reagent_id, gene_id, ordinal ) 
    select reagent_id, vendor_gene_id, 0 AS ordinal
    from silencing_reagent
    where vendor_gene_id is not null;
    
    alter table silencing_reagent
    drop column facility_gene_id;
    
    alter table silencing_reagent
    drop column vendor_gene_id;

COMMIT;
