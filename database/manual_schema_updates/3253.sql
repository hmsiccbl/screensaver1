BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3253,
current_timestamp,
'converted funding support to entity';

create table funding_support (
    funding_support_id int4 not null,
    value text unique,
    primary key (funding_support_id)
);

create sequence funding_support_id_seq;

create table screen_funding_support_link (
    screen_id int4 not null,
    funding_support_id int4 not null,
    primary key (screen_id, funding_support_id)
);

alter table screen_funding_support_link 
    add constraint FKEAA8B25F806C52FD 
    foreign key (screen_id) 
    references screen;

alter table screen_funding_support_link 
    add constraint FKEAA8B25F1FCB31E2 
    foreign key (funding_support_id) 
    references funding_support;

/* TODO: ICCB-L specific!  Modify as necessary for your facility's data.  If your screens specified any of these funding supports, you will need to insert at those that are in use.  */
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Other');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Unspecified');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Clardy Grants');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'D''Andrea CMCR');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'ICCB-L HMS Affiliate');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'ICCB-L HMS Quad (Internal)');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'ICCB-L External');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Mitchison P01');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'ICG');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Marcus Library Screen');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'NERCE/NSRB');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Novartis');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'NSRB-RNAi Quad (internal)');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'NSRB-RNAi HMS Affiliate');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'NSRB-RNAi External');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Sanofi-Aventis');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Yuan NIH 06-07');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'Gates-Elledge (R)');
insert into funding_support (funding_support_id, value) values (nextval('funding_support_id_seq'), 'HHMI-Elledge (SM)');

insert into screen_funding_support_link (screen_id, funding_support_id) select screen_id, funding_support_id from screen_funding_support sfs join funding_support fs on(sfs.funding_support = fs.value);

drop table screen_funding_support;

COMMIT;