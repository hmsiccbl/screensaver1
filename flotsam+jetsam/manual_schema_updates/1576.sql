BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment) 
SELECT 
1576, 
current_timestamp, 
'added property Screen.publishableProtocolComments';
 
ALTER TABLE screen ADD publishable_protocol_comments TEXT;

COMMIT;