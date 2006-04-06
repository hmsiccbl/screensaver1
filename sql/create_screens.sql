/* screensaver/sql/create_screens.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   assay_readout_type_taxonomy
**   screen
**   screen_collaborators
**   screen_keywords
**   status_item
**   abase_testset
**   publication
**   screen_publication_link
**   letter_of_support
**   billing_information
*/


/* table assay_readout_type_taxonomy */

CREATE TABLE assay_readout_type_taxonomy (
  name TEXT PRIMARY KEY
);

COPY assay_readout_type_taxonomy (name) FROM STDIN;
Photometry
Luminescence
Fluorescence Intensity
FP
FRET
Imaging
Unspecified
\.


/* table screen */

CREATE TABLE screen (
  screen_id              SERIAL PRIMARY KEY,
  date_created           DATE NOT NULL,
  screen_type            TEXT NOT NULL,
  lead_screener          INT NOT NULL REFERENCES screening_room_user,
  title                  TEXT NOT NULL,
  data_meeting_scheduled DATE,
  data_meeting_complete  DATE,
  funding_support        TEXT,  
  summary                TEXT,
  comments               TEXT,
  abase_study_id         TEXT,
  abase_protocol_id      TEXT,
  assay_readout_type_1   TEXT NOT NULL REFERENCES assay_readout_type_taxonomy,
  assay_readout_type_2   TEXT NOT NULL REFERENCES assay_readout_type_taxonomy,
  publishable_protocol   TEXT
);

ALTER TABLE screen ADD
  CHECK (screen_type IN (
    'Small Molecule',
    'RNAi'
  ));

ALTER TABLE screen ADD
  CHECK (funding_support IN (
    'Clardy Grants',
    'D\'Andrea CMCR',
    'ICCB-L HMS Internal',
    'ICCB-L External',
    'Mitchison P01',
    'ICG',
    'NERCE/NSRB',
    'Sanofi-Aventis',
    'Other'
  ));

ALTER TABLE screen ADD
  CHECK (assay_readout_type_1 IN (
    SELECT name FROM assay_readout_type_taxonomy
  ));

ALTER TABLE screen ADD
  CHECK (assay_readout_type_2 IN (
    SELECT name FROM assay_readout_type_taxonomy
  ));


/* table screen_collaborators */

CREATE TABLE screen_collaborators (
  screen_id              INT NOT NULL REFERENCES screen,
  screening_room_user_id INT NOT NULL REFERENCES screening_room_user
);


/* table keywords */

CREATE TABLE screen_keywords (
  screen_id  INT NOT NULL REFERENCES screen,
  keyword    TEXT NOT NULL
);


/* table status_item */

CREATE TABLE status_item (
  screen_id    INT NOT NULL REFERENCES screen,
  status_date  DATE NOT NULL,
  status_value TEXT NOT NULL
);

ALTER TABLE status_item ADD
  CHECK (status_value IN (
    'Accepted',
    'Completed',
    'Completed - Duplicate with Ongoing',
    'Dropped - Technical',
    'Dropped - Resources',
    'Never Initiated',
    'Ongoing',
    'Pending',
    'Transferred to Broad'
  ));


/* table abase_testset */

CREATE TABLE abase_testset (
  screen_id    INT NOT NULL REFERENCES screen,
  testset_date DATE,
  testset_name TEXT,
  comments     TEXT
);


/* table publication */

CREATE TABLE publication (
  pubmed_id      TEXT PRIMARY KEY,
  year_published TEXT NOT NULL,
  authors        TEXT NOT NULL,
  title          TEXT NOT NULL
);


/* table screen_publication_link */

CREATE TABLE screen_publication_link (
  screen_id  INT NOT NULL REFERENCES screen,
  pubmed_id  TEXT NOT NULL REFERENCES publication
);


/* table letter_of_support */

CREATE TABLE letter_of_support (
  screen_id    INT NOT NULL REFERENCES screen,
  written_by   TEXT NOT NULL,
  date_written DATE NOT NULL
);


/* table billing_information */

CREATE TABLE billing_information (
  thirtythree_digit_code  TEXT NOT NULL,
  date_provided           DATE NOT NULL,
  date_charged            DATE NOT NULL,
  amount_charged          TEXT NOT NULL,
  initials                TEXT NOT NULL
);
