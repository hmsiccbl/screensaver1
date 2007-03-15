ALTER TABLE screening_room_activity RENAME COLUMN date TO date_of_activity;
ALTER TABLE screening_room_activity ADD COLUMN date_created TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE screening_room_activity ALTER COLUMN date_created SET NOT NULL;