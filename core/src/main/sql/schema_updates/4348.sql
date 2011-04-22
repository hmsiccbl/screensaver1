BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4348,
current_timestamp,
'added screen.fulfilled_lab_cherry_picks_count';

alter table screen add column fulfilled_lab_cherry_picks_count int4;
update screen set fulfilled_lab_cherry_picks_count = 
(select count(*) from cherry_pick_request cpr join lab_cherry_pick lcp using(cherry_pick_request_id) 
join cherry_pick_assay_plate cpap using(cherry_pick_assay_plate_id) 
join cherry_pick_liquid_transfer cplt on(cplt.activity_id=cpap.cherry_pick_liquid_transfer_id)
where cplt.status = 'Successful' and cpr.screen_id = screen.screen_id);
alter table screen alter column fulfilled_lab_cherry_picks_count set not null;

COMMIT;
