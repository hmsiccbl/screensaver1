BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2481,
current_timestamp,
'billing_information changes';

ALTER TABLE billing_information ADD COLUMN to_be_requested BOOL;
ALTER TABLE billing_information ADD COLUMN see_comments BOOL;
UPDATE billing_information SET to_be_requested = CASE WHEN billing_info_to_be_requested = 'Yes' THEN true ELSE false END;
UPDATE billing_information SET see_comments = CASE WHEN billing_info_to_be_requested = 'No, see comments' THEN true ELSE false END;
ALTER TABLE billing_information ALTER COLUMN to_be_requested SET NOT NULL;
ALTER TABLE billing_information ALTER COLUMN see_comment SET NOT NULL;
ALTER TABLE billing_information DROP COLUMN billing_info_to_be_requested;

COMMIT;