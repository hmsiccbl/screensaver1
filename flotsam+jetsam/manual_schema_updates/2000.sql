BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2000,
current_timestamp,
'well_molfile changes for revised Hibernate mapping';

/* note: well_molfile_index legacy index is in both dev and prod, but shouldn't be, based upon previous schema */
DROP INDEX well_molfile_index;

ALTER TABLE well_molfile DROP CONSTRAINT well_molfile_pkey;
ALTER TABLE well_molfile ADD COLUMN ordinal int4;
ALTER TABLE well_molfile ADD CONSTRAINT well_molfile_well_id UNIQUE (well_id);
ALTER TABLE well_molfile ADD PRIMARY KEY (well_id, ordinal);
UPDATE well_molfile SET ordinal = 1;
ALTER TABLE well_molfile ALTER COLUMN molfile SET NOT NULL;
ALTER TABLE well_molfile ALTER COLUMN ordinal SET NOT NULL;

COMMIT;