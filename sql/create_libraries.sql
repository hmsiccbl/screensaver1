/* screensaver/sql/create_libraries.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   library_type_taxonomy
**   screenable_taxonomy
**   library
**   copy
**   plate_type_taxonomy
**   copy_info
**   copy_action
**   well
**   compound
**   well_compound_link
**   compound_synonym
**   compound_cas_number
**   compound_nsc_number
**   gene
**   old_entrezgene_id
**   old_entrezgene_symbol
**   genbank_accession_number
**   sirna
*/


/* table library_type_taxonomy */

CREATE TABLE library_type_taxonomy (
  library_type TEXT PRIMARY KEY
);

COPY library_type_taxonomy (library_type) FROM STDIN;
Commercial
DOS
Annotation
Discrete
Known Bioactives
NCI
Natural Products
RNAi
Other
\.


/* table screenable_taxonomy */

CREATE TABLE screenable_taxonomy (
  screenable TEXT PRIMARY KEY
);

COPY screenable_taxonomy (screenable) FROM STDIN;
Yes
No
Not Recommended
Not Yet Plated
Retired
\.


/* table library */

CREATE TABLE library (
  library_id                 SERIAL PRIMARY KEY,
  name                       TEXT NOT NULL,
  short_name                 TEXT NOT NULL,
  description                TEXT,
  vendor                     TEXT,
  library_type               TEXT NOT NULL REFERENCES library_type_taxonomy,
  start_plate                INT NOT NULL,
  end_plate                  INT NOT NULL,

  /* from the FileMaker libraries database */
  filemaker_number           INT,
  alias                      TEXT,
  screenable                 TEXT REFERENCES screenable_taxonomy,
  compount_count             INT,
  screening_copy             TEXT,
  compound_concentration     TEXT,
  cherry_pick_copy           BOOLEAN,
  date_plated_or_acquired    DATE,
  date_purchased             DATE,
  non_compound_wells         TEXT,
  notes_about_copies         TEXT,
  screening_room_comments    TEXT,
  diversity_set_plates       TEXT,
  mapped_from_copy           TEXT,
  mapped_from_plate          TEXT,
  purchased_using_funds_from TEXT,
  plated_using_funds_from    TEXT,
  informatics_comments       TEXT,
  data_file_location         TEXT,
  chemist_dos                TEXT,
  chemistry_comments         TEXT,
  screening_set              TEXT
);


/* table copy */

CREATE TABLE copy (
  copy_id            SERIAL PRIMARY KEY,
  library_id         INT NOT NULL REFERENCES library,
  name               TEXT NOT NULL,
  has_copy_per_plate BOOLEAN NOT NULL
);


/* table plate_type_taxonomy */

CREATE TABLE plate_type_taxonomy (
  plate_type TEXT PRIMARY KEY
);

COPY plate_type_taxonomy (plate_type) FROM STDIN;
Marsh
ABgene
Genetix
\.


/* table copy_info */

CREATE TABLE copy_info (
  copy_info_id SERIAL PRIMARY KEY,
  copy_id      INT NOT NULL REFERENCES copy,
  plate_number INT NOT NULL,
  location     TEXT NOT NULL,
  plate_type   TEXT NOT NULL REFERENCES plate_type_taxonomy,
  volume       TEXT NOT NULL,
  comments     TEXT,
  date_retired DATE
);


/* table copy_action */

CREATE TABLE copy_action (
  copy_info_id INT NOT NULL REFERENCES copy_info,
  description  TEXT NOT NULL,
  date         DATE NOT NULL
);


/* table well */

CREATE TABLE well (
  well_id           SERIAL PRIMARY KEY,
  library_id        INT NOT NULL REFERENCES library,
  plate_number      INT NOT NULL,
  well_name         TEXT NOT NULL,
  iccb_number       TEXT,
  vendor_identifier TEXT
);


/* table compound */

CREATE TABLE compound (
  compound_id SERIAL PRIMARY KEY,
  name        TEXT NOT NULL,
  smiles      TEXT,
  is_salt     BOOLEAN NOT NULL,
  pubchem_cid TEXT,
  chembank_id TEXT
);


/* table well_compound_link */

CREATE TABLE well_compound_link (
  well_id     INT NOT NULL REFERENCES well,
  compound_id INT NOT NULL REFERENCES compound
);


/* table compound_synonym */

CREATE TABLE compound_synonym (
  compound_id INT NOT NULL REFERENCES compound,
  synonym     TEXT NOT NULL
);


/* table compound_cas_number */

CREATE TABLE compound_cas_number (
  compound_id INT NOT NULL REFERENCES compound,
  cas_number  TEXT NOT NULL
);


/* table compound_nsc_number */

CREATE TABLE compound_nsc_number (
  compound_id INT NOT NULL REFERENCES compound,
  nsc_number  TEXT NOT NULL
);


/* table gene */

CREATE TABLE gene (
  gene_id    SERIAL PRIMARY KEY,
  gene_name  TEXT NOT NULL,
  entrezgene_id INT NOT NULL,
  entrezgene_symbol TEXT NOT NULL,
  species_name      TEXT NOT NULL
);


/* table old_entrezgene_id */

CREATE TABLE old_entrezgene_id (
  gene_id       INT NOT NULL REFERENCES gene,
  entrezgene_id INT NOT NULL
);


/* table old_entrezgene_symbol */

CREATE TABLE old_entrezgene_symbol (
  gene_id           INT NOT NULL REFERENCES gene,
  entrezgene_symbol TEXT NOT NULL
);


/* table genbank_accession_number */

CREATE TABLE genbank_accession_number (
  gene_id                  INT NOT NULL REFERENCES gene,
  genbank_accession_number INT NOT NULL
);


/* table sirna */

CREATE TABLE sirna (
  sirna_id   SERIAL PRIMARY KEY,
  sequence   TEXT,
  vendor_identifier TEXT,
  gene_id           INT NOT NULL REFERENCES gene
);


/* table well_sirna_link */

CREATE TABLE well_sirna_link (
  well_id    INT NOT NULL REFERENCES well,
  sirna_id   INT NOT NULL REFERENCES sirna
);
