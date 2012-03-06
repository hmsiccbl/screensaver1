BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7063,
current_timestamp,
'update gene_symbol to be an ordered list type relation to the gene';


alter table gene_symbol add column ordinal int4;

update gene_symbol gs1 set ordinal = (select count(*)-1 from gene_symbol gs2 where 
	gs2.gene_id = gs1.gene_id and 
 	gs1.entrezgene_symbol <= gs2.entrezgene_symbol order by gs1.entrezgene_symbol );
 	
alter table gene_symbol alter column ordinal set not null;

alter table gene_symbol drop constraint gene_symbol_pkey;	

alter table gene_symbol add constraint gene_symbol_pkey primary key (gene_id, ordinal);

COMMIT;