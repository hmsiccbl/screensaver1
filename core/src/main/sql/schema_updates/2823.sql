BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2823,
current_timestamp,
'add publication.attached_file_id';

ALTER TABLE publication ADD COLUMN attached_file_id INT4 UNIQUE;
ALTER TABLE PUBLICATION 
    ADD CONSTRAINT fk_publication_to_attached_file 
    FOREIGN KEY (attached_file_id) 
    REFERENCES attached_file;

COMMIT;