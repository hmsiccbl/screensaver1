BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1997,
current_timestamp,
'well_molfile changes for revised Hibernate mapping';

ALTER TABLE well_molfile DROP CONSTRAINT well_molfile_pkey;
ALTER TABLE well_molfile ALTER COLUMN molfile SET NOT NULL;
ALTER TABLE well_molfile ADD PRIMARY KEY (well_id, molfile);

COMMIT;