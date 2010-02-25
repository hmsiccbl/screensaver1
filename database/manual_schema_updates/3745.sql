BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3745,
current_timestamp,
'add "Gray Library Screen" funding support';

insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Gray Library Screen');

COMMIT;