/* screensaver/sql/create_screens.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   screen_type_taxonomy
**   funding_support_taxonomy
**   assay_readout_type_taxonomy
**   screen
**   screen_collaborators
**   screen_keywords
**   status_value_taxonomy
**   status_item
**   abase_testset
**   publication
**   screen_publication_link
**   letter_of_support
**   billing_information
**   visit_type_taxonomy
**   visit
**   screen_visit_link
**   assay_protocol_type_taxonomy
**   non_cherry_pick_visit
**   plates_used
**   equipment_used
**   cherry_pick
*/


/* table screen_type_taxonomy */

CREATE TABLE screen_type_taxonomy (
  screen_type TEXT PRIMARY KEY
);

COPY screen_type_taxonomy (screen_type) FROM STDIN;
Small Molecule
RNAi
\.


/* table funding_support_taxonomy */

CREATE TABLE funding_support_taxonomy (
  funding_support TEXT PRIMARY KEY
);

COPY funding_support_taxonomy (funding_support) FROM STDIN;
Clardy Grants
D'Andrea CMCR
ICCB-L HMS Internal
ICCB-L External
Mitchison P01
ICG
NERCE/NSRB
Sanofi-Aventis
Other
\.


/* table assay_readout_type_taxonomy */

CREATE TABLE assay_readout_type_taxonomy (
  assay_readout_type TEXT PRIMARY KEY
);

COPY assay_readout_type_taxonomy (assay_readout_type) FROM STDIN;
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
  screen_type            TEXT NOT NULL REFERENCES screen_type_taxonomy,
  lead_screener          INT NOT NULL REFERENCES screening_room_user,
  title                  TEXT NOT NULL,
  data_meeting_scheduled DATE,
  data_meeting_complete  DATE,
  funding_support        TEXT REFERENCES funding_support_taxonomy,
  summary                TEXT,
  comments               TEXT,
  abase_study_id         TEXT,
  abase_protocol_id      TEXT,
  assay_readout_type_1   TEXT NOT NULL REFERENCES assay_readout_type_taxonomy,
  assay_readout_type_2   TEXT NOT NULL REFERENCES assay_readout_type_taxonomy,
  publishable_protocol   TEXT
);


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


/* table status_value_taxonomy */

CREATE TABLE status_value_taxonomy (
  status_value TEXT PRIMARY KEY
);

COPY status_value_taxonomy (status_value) FROM STDIN;
Accepted
Completed
Completed - Duplicate with Ongoing
Dropped - Technical
Dropped - Resources
Never Initiated
Ongoing
Pending
Transferred to Broad
\.


/* table status_item */

CREATE TABLE status_item (
  screen_id    INT NOT NULL REFERENCES screen,
  status_date  DATE NOT NULL,
  status_value TEXT NOT NULL REFERENCES status_value_taxonomy
);


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
  screen_id               INT NOT NULL REFERENCES screen,
  thirtythree_digit_code  TEXT NOT NULL,
  date_provided           DATE NOT NULL,
  date_charged            DATE NOT NULL,
  amount_charged          TEXT NOT NULL,
  initials                TEXT NOT NULL
);


/* table visit_type_taxonomy */

CREATE TABLE visit_type_taxonomy (
  visit_type TEXT PRIMARY KEY
);

COPY visit_type_taxonomy (visit_type) FROM STDIN;
Library
Cherry Pick
Liquid Handling Only
Special
\.


/* table visit */

CREATE TABLE visit (
  visit_id             SERIAL PRIMARY KEY,
  is_cherry_pick_visit BOOLEAN NOT NULL,
  date_created         DATE NOT NULL,
  performed_by         INT NOT NULL REFERENCES screening_room_user,
  visit_date           DATE NOT NULL,
  visit_type           TEXT NOT NULL REFERENCES visit_type_taxonomy,
  abase_testset_id     TEXT,
  comments             TEXT
);


/* table screen_visit_link */

CREATE TABLE screen_visit_link (
  screen_id  INT NOT NULL REFERENCES screen,
  visit_id   INT NOT NULL REFERENCES visit
);


/* table assay_protocol_type_taxonomy */

CREATE TABLE assay_protocol_type_taxonomy (
  assay_protocol_type TEXT PRIMARY KEY
);

COPY assay_protocol_type_taxonomy (assay_protocol_type) FROM STDIN;
Preliminary
Established
Protocol last modified on
\.


/* table non_cherry_pick_visit */

CREATE TABLE non_cherry_pick_visit (
  visit_id                       INT NOT NULL REFERENCES visit,
  number_of_replicates           INT,
  volume_of_compound_transferred TEXT,
  final_screen_concentration     TEXT,
  assay_protocol_type            TEXT REFERENCES assay_protocol_type_taxonomy,
  assay_date                     DATE,
  assay_protocol                 TEXT NOT NULL
);


/* table plates_used */

CREATE TABLE plates_used (
  visit_id    INT NOT NULL REFERENCES visit,
  start_plate TEXT NOT NULL,
  end_plate   TEXT NOT NULL,
  copy        TEXT NOT NULL
);


/* table equipment_used */

CREATE TABLE equipment_used (
  visit_id    INT NOT NULL REFERENCES visit,
  equipment   TEXT NOT NULL,
  protocol    TEXT NOT NULL,
  description TEXT NOT NULL
);


/* table cherry_pick */

CREATE TABLE cherry_pick (
  visit_id   INT NOT NULL REFERENCES visit,
  well       INT NOT NULL REFERENCES well,
  copy       TEXT NOT NULL,
  volume     TEXT NOT NULL,
  plate_map  TEXT
);
