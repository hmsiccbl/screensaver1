/*
 * NOTE: LINCS feature
 */
BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
6173,
current_timestamp,
'add well_studied to study/screen';

alter table screen add column well_studied_id text;

alter table screen
       add constraint fk_screen_to_well_studied 
       foreign key (well_studied_id) 
       references well;

COMMIT;
