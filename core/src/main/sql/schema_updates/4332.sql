BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4332,
current_timestamp,
'add reagent identifier index';

drop index if exists reagent_vendor_identifier_index;
create index reagent_vendor_identifier_index on reagent (vendor_identifier);

COMMIT;
