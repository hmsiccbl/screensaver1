BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
5327,
current_timestamp,
'remove library.comments';

create temp table library_comment (library_id int, comment text, activity_id int);
insert into library_comment select library_id, comments, nextval('activity_id_seq') from library where comments is not null;

/* TODO: the only library comments that were ever added were for database migration changes; 
 * if this is not the case, it may be more appropriate to migrate the library comments to 
 * 'Comment' administrative activity types, rather than 'Entity Update' */
/* TODO: change admin user ID */
insert into activity (activity_id, version, date_created, comments, date_of_activity, performed_by_id, created_by_id)
select activity_id, 0, now(), comment, '1970-01-01', 3444, 3444 from library_comment;
insert into administrative_activity (activity_id, administrative_activity_type) 
select activity_id, 'Entity Update' from library_comment;
insert into library_update_activity (library_id, update_activity_id) select library_id, activity_id from library_comment;

alter table library drop column comments;

COMMIT;
