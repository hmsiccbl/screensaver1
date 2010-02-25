BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3496,
current_timestamp,
'screen.data_sharing_level replaces screen.is_shareable';

ALTER TABLE screen ADD COLUMN data_sharing_level int4 not null default 3;

/* TODO: modify migration of screen data sharing level, as necessary (0=public, 3=private) */
UPDATE screen SET data_sharing_level = 0 where screen.is_shareable;
UPDATE screen SET data_sharing_level = 3 where not screen.is_shareable;

ALTER TABLE screen DROP COLUMN is_shareable;

ALTER TABLE screen ALTER COLUMN data_sharing_level drop default;

COMMIT;
