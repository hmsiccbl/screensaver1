BEGIN;
CREATE TABLE schema_history (
    screensaver_revision integer NOT NULL,
    date_updated timestamp without time zone,
    "comment" text
);
COMMIT;