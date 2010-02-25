BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3502,
current_timestamp,
'add publication.pubmed_central_id';

alter table publication add column pubmed_central_id int4;

COMMIT;