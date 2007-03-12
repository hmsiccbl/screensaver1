ALTER TABLE screening_room_activity ADD COLUMN microliter_volume_transfered_per_well NUMERIC(19, 2);
ALTER TABLE cherry_pick_liquid_transfer DROP COLUMN actual_microliter_transfer_volume_per_well;
ALTER TABLE library_screening DROP COLUMN volume_of_compound_transferred;

ALTER TABLE screening ADD COLUMN assay_protocol_last_modified_date TIMESTAMP;
ALTER TABLE screening ADD COLUMN assay_protocol_type TEXT;
ALTER TABLE library_screening DROP COLUMN assay_protocol_type;
ALTER TABLE screening ADD COLUMN estimated_final_screen_concentration_in_moles NUMERIC(19, 2);

DROP TABLE legacy_screening_room_activity;
