BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2902,
current_timestamp,
'change screen_billing_item field date_faxed to date_sent_for_billing and make the field nullable';

ALTER TABLE screen_billing_item RENAME COLUMN date_faxed TO date_sent_for_billing; 
ALTER TABLE screen_billing_item ALTER COLUMN date_sent_for_billing DROP NOT NULL;

COMMIT;