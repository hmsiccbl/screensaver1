BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5542,
current_timestamp,
'rename screen.fulfilled_lab_cherry_picks_count to total_plated_lab_cherry_picks';

alter table screen rename fulfilled_lab_cherry_picks_count to total_plated_lab_cherry_picks;

COMMIT;
