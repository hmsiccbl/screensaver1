BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2822,
current_timestamp,
'add screen_billing_items.ordinal';

ALTER TABLE screen_billing_item ADD COLUMN ordinal int4;
ALTER TABLE screen_billing_item DROP CONSTRAINT screen_billing_item_pkey;
UPDATE screen_billing_item SET ordinal = (select count(*) from screen_billing_item sbi where sbi.screen_id = screen_billing_item.screen_id and sbi.date_faxed < screen_billing_item.date_faxed and sbi.item_to_be_charged < screen_billing_item.item_to_be_charged and sbi.amount < screen_billing_item.amount);
ALTER TABLE screen_billing_item ALTER COLUMN ordinal SET NOT NULL;
ALTER TABLE screen_billing_item ADD PRIMARY KEY (screen_id, ordinal);

COMMIT;