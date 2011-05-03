BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2468,
current_timestamp,
'added attached_file properties; deleted letter_of_support';

DROP TABLE letter_of_support;

DROP TABLE attached_file;

CREATE TABLE attached_file (
    attached_file_id INT4 NOT NULL,
    date_created TIMESTAMP NOT NULL,
    file_contents OID NOT NULL,
    file_type TEXT NOT NULL,
    filename TEXT NOT NULL,
    version INT4 NOT NULL,
    screen_id INT4 NOT NULL,
    PRIMARY KEY (attached_file_id),
    UNIQUE (screen_id, filename)
);

ALTER TABLE attached_file
    ADD CONSTRAINT fk_attached_file_to_screen
    FOREIGN KEY (screen_id)
    REFERENCES screen;

COMMIT;
