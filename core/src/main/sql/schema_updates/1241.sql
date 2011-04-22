ALTER TABLE screen ADD all_time_screening_room_activity_count INTEGER;
UPDATE screen SET all_time_screening_room_activity_count = (
  SELECT COUNT(*) FROM screening_room_activity WHERE
  screen.screen_id = screening_room_activity.screen_id);
ALTER TABLE screen ALTER COLUMN all_time_screening_room_activity_count SET NOT NULL;
