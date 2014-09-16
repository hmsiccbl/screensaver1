BEGIN;


INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
20140916,
current_timestamp,
'issue 144 - Add funding source dropdown to the user service activity';

alter table service_activity add column funding_support_id int4;

alter table service_activity 
         add constraint fk_service_activity_to_funding_support 
         foreign key (funding_support_id) 
         references funding_support;
COMMIT;

