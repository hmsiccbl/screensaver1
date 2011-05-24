BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
4447,
current_timestamp,
'updates to remove screen-to-publication direct link, to allow many-to-many link from screen and reagent to publication';

create table reagent_publication_link (
    reagent_id int4 not null,
    publication_id int4 not null unique,
    primary key (reagent_id, publication_id)
);

alter table reagent_publication_link 
    add constraint FKEA69421A95666B57 
    foreign key (publication_id) 
    references publication;

alter table reagent_publication_link 
    add constraint fk_reagent_publication_link_to_reagent 
    foreign key (reagent_id) 
    references reagent;    

create table screen_publication_link (
    screen_id int4 not null,
    publication_id int4 not null unique,
    primary key (screen_id, publication_id)
);

alter table screen_publication_link 
    add constraint FK81A349A095666B57 
    foreign key (publication_id) 
    references publication;

alter table screen_publication_link 
    add constraint fk_screen_publication_link_to_screen 
    foreign key (screen_id) 
    references screen;

update screen_publication_link set screen_id = (select screen_id from publication where screen_id is not null);
update screen_publication_link set publication_id = (select publication_id from publication where screen_id is not null);
        
alter table publication drop constraint fk_publication_to_screen;
alter table publication drop column screen_id;

COMMIT;
