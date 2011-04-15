BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5370,
current_timestamp,
'remove rnai_knockdown_confirmation';

drop table rnai_knockdown_confirmation;
drop sequence rnai_knockdown_confirmation_id_seq;
alter table screener_cherry_pick drop is_hit_confirmed_via_experimentation;
alter table screener_cherry_pick drop notes_on_hit_confirmation;

COMMIT;
