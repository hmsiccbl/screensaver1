BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3496,
current_timestamp,
'screen.data_sharing_level replaces screen.is_shareable and screen_result.is_shareable';


/* set all screens to private access level, by default */
ALTER TABLE screen ADD COLUMN data_sharing_level int4 not null default 3;

/* If a screen or its screen result was flagged as "shareable", set
the screen's new data access level property to shared.  Note:
screen.is_shareable was previously only used to ensure that everyone
could see Studies (which are screens where screen_number >= 100000);
screen.is_shareable was not a property exposed via the web UI;
screen_result.is_shareable was exposed via the web UI, and was used to
mark screen result data as shareable among screeners; there is now
only the screen.data_sharing_level property, so we set this based upon
the values of both screen.is_shareable and screen_result.is_shareable
*/ 
UPDATE screen SET data_sharing_level = 0 /* shared */ where screen.is_shareable or (select sr.is_shareable from screen_result sr where sr.screen_id = screen.screen_id);

ALTER TABLE screen DROP COLUMN is_shareable;
ALTER TABLE screen_result DROP COLUMN is_shareable;
ALTER TABLE screen ALTER COLUMN data_sharing_level drop default;

COMMIT;
