BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1380;
 
ALTER TABLE gene ADD COLUMN gene_id_2 integer;
UPDATE gene SET gene_id_2 = int4(gene_id);
ALTER TABLE gene DROP COLUMN gene_id CASCADE;
ALTER TABLE gene RENAME COLUMN gene_id_2 TO gene_id;
ALTER TABLE gene ADD PRIMARY KEY (gene_id);

ALTER TABLE silencing_reagent ADD COLUMN gene_id_2 integer;
UPDATE silencing_reagent SET gene_id_2 = int4(gene_id);
ALTER TABLE silencing_reagent DROP COLUMN gene_id;
ALTER TABLE silencing_reagent RENAME COLUMN gene_id_2 TO gene_id;
ALTER TABLE silencing_reagent ALTER COLUMN gene_id SET NOT NULL;

ALTER TABLE gene_old_entrezgene_id ADD COLUMN gene_id_2 integer;
UPDATE gene_old_entrezgene_id SET gene_id_2 = int4(gene_id);
ALTER TABLE gene_old_entrezgene_id DROP COLUMN gene_id;
ALTER TABLE gene_old_entrezgene_id RENAME COLUMN gene_id_2 TO gene_id;
ALTER TABLE gene_old_entrezgene_id ALTER COLUMN gene_id SET NOT NULL;
ALTER TABLE gene_old_entrezgene_id ADD CONSTRAINT gene_old_entrezgene_id_pkey PRIMARY KEY (gene_id, old_entrezgene_id);

ALTER TABLE gene_old_entrezgene_symbol ADD COLUMN gene_id_2 integer;
UPDATE gene_old_entrezgene_symbol SET gene_id_2 = int4(gene_id);
ALTER TABLE gene_old_entrezgene_symbol DROP COLUMN gene_id;
ALTER TABLE gene_old_entrezgene_symbol RENAME COLUMN gene_id_2 TO gene_id;
ALTER TABLE gene_old_entrezgene_symbol ALTER COLUMN gene_id SET NOT NULL;
ALTER TABLE gene_old_entrezgene_symbol ADD CONSTRAINT gene_old_entrezgene_symbol_pkey PRIMARY KEY (gene_id, old_entrezgene_symbol);

ALTER TABLE gene_genbank_accession_number ADD COLUMN gene_id_2 integer;
UPDATE gene_genbank_accession_number SET gene_id_2 = int4(gene_id);
ALTER TABLE gene_genbank_accession_number DROP COLUMN gene_id;
ALTER TABLE gene_genbank_accession_number RENAME COLUMN gene_id_2 TO gene_id;
ALTER TABLE gene_genbank_accession_number ALTER COLUMN gene_id SET NOT NULL;
ALTER TABLE gene_genbank_accession_number ADD CONSTRAINT gene_genbank_accession_number_pkey PRIMARY KEY (gene_id, genbank_accession_number);

alter table silencing_reagent 
    add constraint fk_silencing_reagent_to_gene 
    foreign key (gene_id) 
    references gene;

alter table gene_old_entrezgene_id 
    add constraint fk_gene_old_entrezgene_id_to_gene 
    foreign key (gene_id) 
    references gene;

alter table gene_old_entrezgene_symbol 
    add constraint fk_gene_old_entrezgene_symbol_to_gene 
    foreign key (gene_id) 
    references gene;

alter table gene_genbank_accession_number 
    add constraint fk_gene_genbank_accession_number_to_gene 
    foreign key (gene_id) 
    references gene;

COMMIT;
