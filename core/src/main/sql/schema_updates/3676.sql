BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3676,
current_timestamp,
'change lab_affiliation_id to be surrogate key; make cherry pick request auditable; remove screen.lead_screener_id not null constraint';


/* change lab_affiliation_id to be surrogate key */

drop sequence if exists lab_affiliation_id_seq;
create sequence lab_affiliation_id_seq;
alter table lab_affiliation drop lab_affiliation_id cascade;
alter table lab_affiliation add lab_affiliation_id int4;
update lab_affiliation set lab_affiliation_id = nextval('lab_affiliation_id_seq');
alter table lab_affiliation add primary key (lab_affiliation_id);

alter table lab_head rename lab_affiliation_id to lab_affiliation_name;
alter table lab_head add lab_affiliation_id int4;
update lab_head set lab_affiliation_id = la.lab_affiliation_id from lab_affiliation la where la.affiliation_name = lab_head.lab_affiliation_name;
alter table lab_head drop lab_affiliation_name;
alter table lab_head 
    add constraint fk_lab_head_to_lab_affiliation 
    foreign key (lab_affiliation_id) 
    references lab_affiliation;


/* make cherry pick request auditable */

alter table cherry_pick_request add date_created timestamp;
update cherry_pick_request set date_created = cast(date_requested as timestamp);
alter table cherry_pick_request alter date_created set not null;


alter table cherry_pick_request add created_by_id int4;
alter table cherry_pick_request 
    add constraint FKA0AF7D5766AB751E 
    foreign key (created_by_id) 
    references screensaver_user;

create table cherry_pick_request_update_activity (
    cherry_pick_request_id int4 not null,
    update_activity_id int4 not null unique,
    primary key (cherry_pick_request_id, update_activity_id)
);
alter table cherry_pick_request_update_activity 
    add constraint FKE60B02DDACAEF6AE 
    foreign key (update_activity_id) 
    references administrative_activity;
alter table cherry_pick_request_update_activity 
    add constraint FKE60B02DDCEAC5AA7 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;


/* remove screen.lead_screener_id not null constraint */

alter table screen alter lead_screener_id drop not null;


COMMIT;