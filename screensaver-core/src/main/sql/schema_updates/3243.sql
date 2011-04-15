BEGIN;

alter table library
add column owner_screener_id integer;

alter table library add
  CONSTRAINT fk_library_to_owner FOREIGN KEY (owner_screener_id)
      REFERENCES screening_room_user (screensaver_user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
 
INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3243,
current_timestamp,
'Added column owner to table library.';     
      
COMMIT;