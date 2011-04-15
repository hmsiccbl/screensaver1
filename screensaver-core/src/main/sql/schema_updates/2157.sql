BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2157,
current_timestamp,
'add result_value.rvt_and_positive, well.gene_id indexes';

CREATE INDEX result_value_rvt_and_positive_index ON result_value (result_value_type_id, is_positive);
CREATE INDEX well_gene_id_index ON well (gene_id);


COMMIT;