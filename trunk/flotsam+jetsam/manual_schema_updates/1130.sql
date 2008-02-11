ALTER TABLE billing_information
  RENAME COLUMN is_fee_to_be_charged_for_screening
  TO billing_info_to_be_requested;
ALTER TABLE billing_information
  ADD COLUMN billing_for_supplies_only BOOL;
ALTER TABLE billing_information
  ADD COLUMN billing_info_return_date DATE;
ALTER TABLE billing_information
  ADD COLUMN facilities_and_administration_charge TEXT;
