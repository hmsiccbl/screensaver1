BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2474,
current_timestamp,
'billing_information, billing_item amounts to numeric types';

ALTER TABLE billing_information RENAME amount_to_be_charged_for_screen TO amount_to_be_charged_for_screen_OLD;
ALTER TABLE billing_information ADD COLUMN amount_to_be_charged_for_screen NUMERIC(9, 2);
UPDATE billing_information SET amount_to_be_charged_for_screen = CAST(amount_to_be_charged_for_screen_OLD as numeric(9,2));
ALTER TABLE billing_information DROP COLUMN amount_to_be_charged_for_screen_OLD;

ALTER TABLE billing_information RENAME facilities_and_administration_charge TO facilities_and_administration_charge_OLD;
ALTER TABLE billing_information ADD COLUMN facilities_and_administration_charge NUMERIC(9, 2);
UPDATE billing_information SET facilities_and_administration_charge = CAST(facilities_and_administration_charge_OLD as numeric(9,2));
ALTER TABLE billing_information DROP COLUMN facilities_and_administration_charge_OLD;

ALTER TABLE billing_item RENAME amount TO amount_OLD;
ALTER TABLE billing_item ADD COLUMN amount NUMERIC(9, 2);
UPDATE billing_item SET amount = CAST(amount_OLD as numeric(9,2));
ALTER TABLE billing_item DROP COLUMN amount_OLD;

COMMIT;