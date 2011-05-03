BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5086,
current_timestamp,
'add plate size to eppendorf plate type, correct bad "ABGene" plate_type values';

/* note: incorrect "ABGene" plate_type values were introduced by bug in 4026.sql; the 'G' should have been lowercase */

update plate set plate_type = 'ABgene 384 CB PP' where plate_type in ('ABgene', 'ABGene');
update plate set plate_type = 'Costar 96 RB PS' where plate_type = 'Costar';
update plate set plate_type = 'Eppendorf 384 CB PP' where plate_type = 'Eppendorf';
update plate set plate_type = 'Genetix 384 CB PP' where plate_type = 'Genetix';
update plate set plate_type = 'Marsh 384 VB PP' where plate_type = 'Marsh';
update plate set plate_type = 'Nunc 96 VB PS' where plate_type = 'Nunc';

update cherry_pick_assay_plate set assay_plate_type = 'ABgene 384 CB PP' where assay_plate_type = 'ABgene';
update cherry_pick_assay_plate set assay_plate_type = 'Costar 96 RB PS' where assay_plate_type = 'Costar';
update cherry_pick_assay_plate set assay_plate_type = 'Eppendorf 384 CB PP' where assay_plate_type = 'Eppendorf';
update cherry_pick_assay_plate set assay_plate_type = 'Genetix 384 CB PP' where assay_plate_type = 'Genetix';
update cherry_pick_assay_plate set assay_plate_type = 'Marsh 384 VB PP' where assay_plate_type = 'Marsh';
update cherry_pick_assay_plate set assay_plate_type = 'Nunc 96 VB PS' where assay_plate_type = 'Nunc';

update cherry_pick_request set assay_plate_type = 'ABgene 384 CB PP' where assay_plate_type = 'ABgene';
update cherry_pick_request set assay_plate_type = 'Costar 96 RB PS' where assay_plate_type = 'Costar';
update cherry_pick_request set assay_plate_type = 'Eppendorf 384 CB PP' where assay_plate_type = 'Eppendorf';
update cherry_pick_request set assay_plate_type = 'Genetix 384 CB PP' where assay_plate_type = 'Genetix';
update cherry_pick_request set assay_plate_type = 'Marsh 384 VB PP' where assay_plate_type = 'Marsh';
update cherry_pick_request set assay_plate_type = 'Nunc 96 VB PS' where assay_plate_type = 'Nunc';

COMMIT;
