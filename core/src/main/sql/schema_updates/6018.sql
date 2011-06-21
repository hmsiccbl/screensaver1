BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6018,
current_timestamp,
'add is_restricted_structure to small_molecule_reagent; add is_restricted_sequence to silencing_reagent';

alter table small_molecule_reagent add is_restricted_structure bool not null default false;
alter table small_molecule_reagent alter is_restricted_structure drop default;

alter table silencing_reagent add is_restricted_sequence bool not null default false;
alter table silencing_reagent alter is_restricted_sequence drop default;

COMMIT;
