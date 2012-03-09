BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7063,
current_timestamp,
'update gene_symbol to be an ordered list type relation to the gene';


alter table gene_symbol add column ordinal int4;

UPDATE gene_symbol gs1
SET ordinal = (SELECT COUNT(1)
               FROM gene_symbol gs2
               WHERE gs2.gene_id = gs1.gene_id
                 AND gs2.entrezgene_symbol < gs1.entrezgene_symbol)
 	
alter table gene_symbol alter column ordinal set not null;

alter table gene_symbol drop constraint gene_symbol_pkey;	

alter table gene_symbol add constraint gene_symbol_pkey primary key (gene_id, ordinal);

COMMIT;