BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
1835,
current_timestamp,
'added study.{is_shareable,is_downloadable,url); annotation_value.numeric_value type changed; annotation_value.reagent_identifier renamed to vendor_identifier; well_vendor_identifier index';

ALTER TABLE screen ADD is_shareable bool;
ALTER TABLE screen ADD is_downloadable bool;
ALTER TABLE screen ADD url text;
UPDATE screen SET is_shareable = true;
UPDATE screen SET is_downloadable = true;
ALTER TABLE screen ALTER is_shareable SET NOT NULL;
ALTER TABLE screen ALTER is_downloadable SET NOT NULL;

ALTER TABLE annotation_value ALTER numeric_value TYPE float8;
ALTER TABLE annotation_value RENAME reagent_identifier TO vendor_identifier;

CREATE INDEX well_vendor_identifier ON well (vendor_identifier);


COMMIT;