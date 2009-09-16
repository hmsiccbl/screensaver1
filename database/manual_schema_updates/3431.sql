/*
 * WARNING: This is a major update to the schema!  Review the changes below, modify as necessary, and test rigorously on your own data!
 */

BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3431,
current_timestamp,
'migration of library contents for v1.8.0';

alter table lab_cherry_pick add constraint lab_cherry_pick_cherry_pick_request_id_key unique (cherry_pick_request_id, source_well_id);

alter table compound_cherry_pick_request drop constraint fk_compound_cherry_pick_request_to_cherry_pick_request;
alter table compound_cherry_pick_request rename to small_molecule_cherry_pick_request;
alter table small_molecule_cherry_pick_request 
    add constraint fk_small_molecule_cherry_pick_request_to_cherry_pick_request 
    foreign key (cherry_pick_request_id) 
    references cherry_pick_request;

drop table derivative cascade;
drop table derivative_screen_result cascade;
drop table derivative_synonym cascade;
drop sequence derivative_id_seq;
drop sequence derivative_screen_result_id_seq;

ALTER TABLE library DROP COLUMN alias;
ALTER TABLE library DROP COLUMN chemistdos;
ALTER TABLE library DROP COLUMN chemistry_comments;
ALTER TABLE library DROP COLUMN cherry_pick_copy;
ALTER TABLE library DROP COLUMN compound_count;
ALTER TABLE library DROP COLUMN compound_concentration_in_screening_copy;
ALTER TABLE library DROP COLUMN data_file_location;
ALTER TABLE library DROP COLUMN diversity_set_plates;
ALTER TABLE library DROP COLUMN mapped_from_copy;
ALTER TABLE library DROP COLUMN mapped_from_plate;
ALTER TABLE library DROP COLUMN non_compound_wells;
ALTER TABLE library DROP COLUMN plating_funds_supplied_by;
ALTER TABLE library DROP COLUMN purchased_using_funds_from;
ALTER TABLE library DROP COLUMN screening_copy;
ALTER TABLE library DROP COLUMN screening_room_comments;
ALTER TABLE library DROP COLUMN screening_set;
ALTER TABLE library RENAME informatics_comments TO comments;

ALTER TABLE library ADD COLUMN latest_released_contents_version_id int4;
ALTER TABLE library ADD COLUMN experimental_well_count int4;
ALTER TABLE library ADD COLUMN is_pool bool default false;

create sequence gene_id_seq;
create sequence reagent_id_seq;
create sequence library_contents_version_id_seq;

create table library_contents_version (
    library_contents_version_id int4 not null,
    version int4 not null,
    date_created timestamp not null,
    version_number int4 not null,
    library_id int4 not null,
    library_contents_loading_activity_id int4 not null,
    library_contents_release_activity_id int4,
    primary key (library_contents_version_id),
    unique (library_id, version_number)
);

insert into library_contents_version 
(library_contents_version_id, version, date_created, version_number, library_id, library_contents_loading_activity_id)
select nextval('library_contents_version_id_seq'), 0, now(), 1, library_id, nextval('activity_id_seq') from library l;

/* insert lcv loading admin activities */
insert into activity (activity_id, version, date_created, comments, performed_by_id, date_of_activity)
select library_contents_loading_activity_id, 1, now(), 'original library contents (migration)', 755, now() 
from library_contents_version lcv;

insert into administrative_activity (activity_id, administrative_activity_type)
select library_contents_loading_activity_id, 'Library Contents Loading'
from library_contents_version lcv;

/* insert lcv released admin activities */
update library_contents_version set library_contents_release_activity_id = nextval('activity_id_seq');

insert into activity (activity_id, version, date_created, comments, performed_by_id, date_of_activity) 
select library_contents_release_activity_id, 1, now(), 'automated release (migration)', 755, now() 
from library_contents_version;

insert into administrative_activity (activity_id, administrative_activity_type) 
select library_contents_release_activity_id, 'Library Contents Loading' 
from library_contents_version;

/* set libraries' latest released contents version */
update library set latest_released_contents_version_id =
(select max(library_contents_version_id) from library_contents_version lcv where lcv.library_id = library.library_id);

alter table well rename column well_type to library_well_type;
alter table well add column latest_released_reagent_id int4;

update library set experimental_well_count = (select count(*) from library l join well w using(library_id) where l.library_id = library.library_id and w.library_well_type = 'experimental');

alter table reagent rename to reagent_old;/* (
    reagent_id text not null,
    version int4 not null,
    primary key (reagent_id)
)*/
create table reagent (
    reagent_id int4 not null,
    vendor_identifier text,
    vendor_name text,
    library_contents_version_id int4 not null,
    well_id text not null,
    primary key (reagent_id),
    unique (well_id, library_contents_version_id)    
);

/* create reagents for all reagent vendor IDs */
insert into reagent (reagent_id, vendor_name, vendor_identifier, library_contents_version_id, well_id)
select nextval('reagent_id_seq'), 
substr(reagent_id, 1, strpos(reagent_id, ':')-1), substr(reagent_id, strpos(reagent_id, ':')+1), 
library_contents_version_id, well_id
from well w join reagent_old ro using(reagent_id) join library_contents_version lcv using (library_id);

/* 
 * create reagents for wells that were missing vendor-{names,ids}, but have a smiles string 
 * Notes:
 * - No (old) well has a compound if well.smiles is null, so we only need to check smiles
 * - 1540:B02 is only well that has a well.smiles, but no compounds (TODO)
*/
insert into reagent (reagent_id, library_contents_version_id, well_id)
select nextval('reagent_id_seq'), 
library_contents_version_id, well_id
from well w join library_contents_version lcv using (library_id) where w.smiles is not null and w.reagent_id is null;

create table natural_product_reagent (
    reagent_id int4 not null,
    primary key (reagent_id)
);
insert into natural_product_reagent (reagent_id)
select reagent_id from reagent r join library_contents_version lcv using (library_contents_version_id) 
join library l using(library_id) 
where l.library_type = 'Natural Products';

alter table silencing_reagent rename to silencing_reagent_old;
create table silencing_reagent (
    reagent_id int4 not null,
    sequence text,
    silencing_reagent_type text,
    vendor_gene_id int4 unique,
    facility_gene_id int4 unique,
    primary key (reagent_id)
);
create table silencing_reagent_duplex_wells (
    silencing_reagent_id int4 not null,
    well_id text not null,
    primary key (silencing_reagent_id, well_id)
);

insert into silencing_reagent (reagent_id, facility_gene_id, vendor_gene_id, sequence, silencing_reagent_type)
select r.reagent_id, nextval('gene_id_seq'), nextval('gene_id_seq'), 
flatten(sro.sequence), min(sro.silencing_reagent_type)
from silencing_reagent_old sro join well_silencing_reagent_link wsrl using(silencing_reagent_id) 
join reagent r using(well_id) 
group by r.reagent_id;

alter table gene rename to gene_old;
/*
create table gene (
    gene_id int4 not null,
    entrezgene_id int4 not null unique,
    entrezgene_symbol text not null,
    gene_name text not null,
    species_name text not null,
    version int4 not null,
    primary key (gene_id)
);
*/
create table gene (
    gene_id int4 not null,
    entrezgene_id int4,
    gene_name text,
    species_name text,
    primary key (gene_id)
);
create table gene_symbol (
    gene_id int4 not null,
    entrezgene_symbol text not null,
    primary key (gene_id, entrezgene_symbol)
);
alter table gene_genbank_accession_number rename to gene_genbank_accession_number_old;
create table gene_genbank_accession_number (
    gene_id int4 not null,
    genbank_accession_number text not null,
    primary key (gene_id, genbank_accession_number)
);

insert into gene (gene_id, entrezgene_id, gene_name, species_name) 
select sr.facility_gene_id, go.entrezgene_id, go.gene_name, go.species_name
from silencing_reagent sr join reagent r using(reagent_id) join well w using(well_id) join gene_old go using(gene_id);

insert into gene_symbol (gene_id, entrezgene_symbol)
select sr.facility_gene_id, go.entrezgene_symbol
from silencing_reagent sr join reagent r using(reagent_id) join well w using(well_id) join gene_old go using(gene_id);

insert into gene_genbank_accession_number (gene_id, genbank_accession_number)
select sr.facility_gene_id, ggano.genbank_accession_number
from silencing_reagent sr join reagent r using(reagent_id) join well w using(well_id) join gene_genbank_accession_number_old ggano using(gene_id);

/* gene name, symbol, and species are replaced ("annotated" data) in old schema, so can't claim they're from vendor */
insert into gene (gene_id, entrezgene_id) 
select sr.vendor_gene_id, go.entrezgene_id
from silencing_reagent sr join reagent r using(reagent_id) join well w using(well_id) join gene_old go using(gene_id);

insert into gene_genbank_accession_number (gene_id, genbank_accession_number)
select sr.vendor_gene_id, ggano.genbank_accession_number
from silencing_reagent sr join reagent r using(reagent_id) join well w using(well_id) join gene_genbank_accession_number_old ggano using(gene_id);
    
create table small_molecule_reagent (
    reagent_id int4 not null,
    inchi text,
    molecular_formula text,
    molecular_mass numeric(15, 9),
    molecular_weight numeric(15, 9),
    smiles text,
    primary key (reagent_id)
);

create table small_molecule_chembank_id (
    reagent_id int4 not null,
    chembank_id int4 not null,
    primary key (reagent_id, chembank_id)
);

create table small_molecule_compound_name (
    reagent_id int4 not null,
    compound_name text not null,
    primary key (reagent_id, compound_name)
);

create table small_molecule_pubchem_cid (
    reagent_id int4 not null,
    pubchem_cid int4 not null,
    primary key (reagent_id, pubchem_cid)
);

/* Note: we use w.smiles not c.smiles, since the former is the
"composite" smiles, if well has multiple compound records */
/* Note: we cannot generate a proper composite inchi string from
multiple individual inchi strings, using only simple SQL functions; so
we'll simply concatentate together the inchi strings to maintain the
legacy inchi data; future library contents versions will correct this */
insert into small_molecule_reagent (reagent_id, smiles, inchi /*, molecular_formula, molecular_mass, molecular_weight*/)
select r.reagent_id, w.smiles, flatten(c.inchi)
from reagent r join well w using(well_id) join well_compound_link wcl using(well_id) join compound c using(compound_id)
group by r.reagent_id, w.smiles;

insert into small_molecule_chembank_id (reagent_id, chembank_id) 
select r.reagent_id, cast(c.chembank_id as int)
from reagent r join well w using(well_id) join well_compound_link wcl using(well_id) 
join compound_chembank_id c using(compound_id)
/* eliminate dups */
group by r.reagent_id, c.chembank_id;

insert into small_molecule_compound_name (reagent_id, compound_name) 
select r.reagent_id, c.compound_name
from reagent r join well w using(well_id) join well_compound_link wcl using(well_id) 
join compound_compound_name c using(compound_id)
/* eliminate dups */
group by r.reagent_id, c.compound_name;

insert into small_molecule_pubchem_cid (reagent_id, pubchem_cid) 
select r.reagent_id, cast(c.pubchem_cid as int)
from reagent r join well w using(well_id) join well_compound_link wcl using(well_id) 
join compound_pubchem_cid c using(compound_id)
/* eliminate dups */
group by r.reagent_id, c.pubchem_cid;

/* molfile */
alter table well_molfile drop constraint FKC3CBA115433A43AB ;
alter table well_molfile rename to molfile;
alter table molfile add column reagent_id int4;
update molfile set reagent_id = (select reagent_id from reagent where well_id = molfile.well_id);
alter table molfile alter column reagent_id set not null;
alter table molfile drop constraint well_molfile_pkey;
alter table molfile drop column well_id;
alter table molfile add primary key (reagent_id, ordinal);
alter table molfile add constraint molfile_reagent_id_key unique (reagent_id);


/* Study and Annotation links to reagent */

alter table annotation_value rename to annotation_value_old;
alter table annotation_value_old drop constraint annotation_value_pkey;
drop index annotation_value_value_index;
drop index annotation_value_numeric_value_index;
drop index annotation_value_reagent_id_index;
alter table study_reagent_link rename to study_reagent_link_old;

create table annotation_value (
    annotation_value_id int4 not null,
    numeric_value float8,
    value text,
    annotation_type_id int4 not null,
    reagent_id int4 not null,
    primary key (annotation_value_id),
    unique (annotation_type_id, reagent_id)
);

create table study_reagent_link (
    study_id int4 not null,
    reagent_id int4 not null,
    primary key (study_id, reagent_id)
);

insert into annotation_value (annotation_value_id, numeric_value, value, annotation_type_id, reagent_id)
select nextval('annotation_value_id_seq'), numeric_value, value, annotation_type_id, r.reagent_id
from annotation_value_old avo join well w using(reagent_id) join reagent r using (well_id);

insert into study_reagent_link (study_id, reagent_id)
select study_id, reagent_id
from screen s join annotation_type at on(s.screen_id = at.study_id) 
join annotation_value av using(annotation_type_id)
group by study_id, reagent_id;


/* add new constraints */

alter table small_molecule_chembank_id 
    add constraint fk_small_molecule_chembank_id_to_small_molecule_reagent 
    foreign key (reagent_id) 
    references small_molecule_reagent;

alter table small_molecule_compound_name 
    add constraint fk_small_molecule_compound_name_id_to_small_molecule_reagent 
    foreign key (reagent_id) 
    references small_molecule_reagent;

alter table small_molecule_pubchem_cid 
    add constraint fk_small_molecule_pubchem_id_to_small_molecule_reagent 
    foreign key (reagent_id) 
    references small_molecule_reagent;

alter table gene_symbol 
    add constraint fk_gene_symbol_to_gene 
    foreign key (gene_id) 
    references gene;

alter table library_contents_version 
    add constraint fk_library_contents_version_to_library 
    foreign key (library_id) 
    references library;
alter table library_contents_version 
    add constraint FK4F9A4FB7426C6700 
    foreign key (library_contents_loading_activity_id) 
    references administrative_activity;
alter table library_contents_version 
    add constraint FK4F9A4FB77D09D54B 
    foreign key (library_contents_release_activity_id) 
    references administrative_activity;

alter table natural_product_reagent 
    add constraint FKC0F2D4C161EA629 
    foreign key (reagent_id) 
    references reagent;

alter table reagent 
    add constraint fk_reagent_to_library_contents_version 
    foreign key (library_contents_version_id) 
    references library_contents_version;
alter table reagent 
    add constraint fk_reagent_to_well 
    foreign key (well_id) 
    references well;

alter table silencing_reagent 
    add constraint FKBA0F32912160CE54 
    foreign key (vendor_gene_id) 
    references gene;
alter table silencing_reagent 
    add constraint FKBA0F3291B09B0CAF
    foreign key (facility_gene_id) 
    references gene;
alter table silencing_reagent 
    add constraint FKBA0F3291161EA629 
    foreign key (reagent_id) 
    references reagent;

alter table silencing_reagent_duplex_wells 
    add constraint FK4769F14433A43AB 
    foreign key (well_id) 
    references well;

alter table silencing_reagent_duplex_wells 
    add constraint FK4769F14DB917E6E 
    foreign key (silencing_reagent_id) 
    references silencing_reagent;

alter table small_molecule_reagent 
    add constraint FKF5A7B431161EA629 
    foreign key (reagent_id) 
    references reagent;

alter table molfile 
    add constraint FK499307862F5B5BE 
    foreign key (reagent_id) 
    references small_molecule_reagent;

alter table well 
    add constraint FK37A0CE68C7E7C9 
    foreign key (latest_released_reagent_id) 
    references reagent;

alter table annotation_value 
    add constraint fk_annotation_value_to_reagent 
    foreign key (reagent_id) 
    references reagent;
alter table annotation_value 
    add constraint fk_annotation_value_to_annotation_type 
    foreign key (annotation_type_id) 
    references annotation_type;

alter table study_reagent_link 
    add constraint fk_reagent_link_to_study 
    foreign key (study_id) 
    references screen;

alter table study_reagent_link 
    add constraint fk_reagent_to_study 
    foreign key (reagent_id) 
    references reagent;

create index annotation_value_value_index on annotation_value (value);
create index annotation_value_numeric_value_index on annotation_value (numeric_value);
create index annotation_value_reagent_id_index on annotation_value (reagent_id);



/* drop old data */

drop table compound_cas_number;
drop table compound_pubchem_cid cascade;
drop table compound_chembank_id cascade;
drop table compound_compound_name cascade;
drop table compound_nsc_number;

drop index well_reagent_id_index;
drop index well_gene_id_index;

/* TODO: migrate data in this table before dropping */
/*drop table gene_old_entrezgene_id cascade*/

/* TODO: migrate data in this table before dropping */
/*drop table gene_old_entrezgene_symbol cascade*/

/* TODO: migrate data in this table before dropping */
/*drop table silencing_reagent_non_targetted_genbank_accession_number cascade*/

drop table well_silencing_reagent_link cascade;

/* TODO: ensure all RNAi wells that had a gene have a reagent: 
  select l.library_name, count(*) from well w join library l using(library_id) 
  where l.screen_type = 'RNAi' and w.gene_id is not null and w.reagent_id is null 
  group by l.library_name;
*/

/* TODO: ensure all Small Molecule wells that had a smiles have a reagent: 
  select l.library_name, count(*) from well w join library l using(library_id) 
  where l.screen_type = 'Small Molecule' and w.smiles is not null and w.reagent_id is null 
  group by l.library_name;
*/
alter table well drop column reagent_id;
alter table well drop column gene_id;
alter table well drop column genbank_accession_number;
alter table well drop column smiles;

drop table well_compound_link cascade;
drop table compound cascade;

drop table silencing_reagent_old cascade;
drop table reagent_old cascade; 
drop table gene_old cascade;
drop table gene_genbank_accession_number_old; 
drop table annotation_value_old; 
drop table study_reagent_link_old;

/* TODO: this is only a heuristic; update as necessary to work with your facility's libraries */
update library set is_pool = true where screen_type = 'RNAi' and library_name like '%Pool%'

/* create pool-to-duplex mapping (works for ICCB-L RNAI libraries only, due to dependency on specific library naming conventions) */
/*
insert into silencing_reagent_duplex_wells (silencing_reagent_id, well_id)
select psr.reagent_id, dw.well_id
from
silencing_reagent psr join reagent pr using(reagent_id) join well pw using(well_id) join library pl using(library_id) join gene pg on(pg.gene_id=psr.vendor_gene_id),
library dl join well dw using(library_id) join reagent dr using(well_id) join silencing_reagent dsr using(reagent_id) join gene dg on(dg.gene_id=dsr.vendor_gene_id)
where 
pl.library_name like '%Pool%' and pl.screen_type = 'RNAi' and
dl.library_name like '%Duplex%' and dl.screen_type = 'RNAi' and
substring(dl.library_name from 1 for 6) = substring(pl.library_name from 1 for 6) and
pg.entrezgene_id=dg.entrezgene_id;
*/

/* create pool-to-duplex mapping for non-conventionally-named libraries */
insert into silencing_reagent_duplex_wells (silencing_reagent_id, well_id)
select psr.reagent_id, dw.well_id
from
silencing_reagent psr join reagent pr using(reagent_id) join well pw using(well_id) join library pl using(library_id) join gene pg on(pg.gene_id=psr.vendor_gene_id),
library dl join well dw using(library_id) join reagent dr using(well_id) join silencing_reagent dsr using(reagent_id) join gene dg on(dg.gene_id=dsr.vendor_gene_id)
where 
pl.library_name not like '%Pool%' and pl.screen_type = 'RNAi' and pl.is_pool and
dl.library_name like '%Duplex%' and dl.screen_type = 'RNAi' and
pg.entrezgene_id=dg.entrezgene_id;

/* TODO: ensure all reagent tuples have an associated {small_molecule,silencing,natural_product}_reagent tuple:
  select l.short_name, count(*) from library l join well w
  using(library_id) join reagent r using(well_id) left join
  small_molecule_reagent r2 on(r.reagent_id=r2.reagent_id) left join
  silencing_reagent r3 on(r.reagent_id=r3.reagent_id) left join
  natural_product_reagent r4 on(r4.reagent_id = r.reagent_id) where
  r2.reagent_id is null and r3.reagent_id is null and r4.reagent_id is
  null and l.screen_type = 'Small Molecule' and l.library_type <>
  'Natural Products' group by l.short_name;
 */
/* fix missing small molecule reagents (due to missing compounds in old schema) */
insert into small_molecule_reagent (reagent_id) select r.reagent_id from library l join well w using(library_id) join reagent r using(well_id) left join small_molecule_reagent r2 on(r.reagent_id=r2.reagent_id) where r2.reagent_id is null and l.screen_type = 'Small Molecule' and l.library_type <> 'Natural Products';

/* pre-condition: ensure both vendor name and identifier are specified together:
  select count(*) from reagent where (vendor_name is null and
  vendor_identifier is not null) or (vendor_name is not null and
  vendor_identifier is null);
*/

/* pre-condition: verify that exactly 4 duplex wells exist for each pool; report those that don't:
    select pw.well_id, count(*) from well pw join reagent r
    using(well_id) join silencing_reagent_duplex_wells l
    on(l.silencing_reagent_id=r.reagent_id) group by pw.well_id having
    count(*) <> 4;
*/

/* set wells' latest released regeant */
analyze;
update well set latest_released_reagent_id = 
    (select reagent_id from reagent r join library_contents_version lcv using(library_contents_version_id)  
     join library l using(library_id) 
      where l.latest_released_contents_version_id = lcv.library_contents_version_id and r.well_id = well.well_id);

COMMIT;