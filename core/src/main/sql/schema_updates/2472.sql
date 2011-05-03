BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2472,
current_timestamp,
'updated publication with new properties, all nullable';

DROP TABLE publication;

CREATE TABLE publication (
    publication_id INT4 NOT NULL,
    authors TEXT,
    journal TEXT,
    pages TEXT,
    pubmed_id TEXT,
    title TEXT,
    version INT4 NOT NULL,
    volume TEXT,
    year_published TEXT,
    screen_id INT4 NOT NULL,
    PRIMARY KEY (publication_id)
);
ALTER TABLE publication 
    ADD CONSTRAINT fk_publication_to_screen 
    FOREIGN KEY (screen_id) 
    REFERENCES screen;

COMMIT;
