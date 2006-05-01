/* screensaver/sql/create_users.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   screensaver_user
**   screensaver_user_role
**   screensaver_user_role_link
**   user_classification_taxonomy
**   lab_affiliation_category_taxonomy
**   screening_room_user
**   checklist_item_type
**   checklist_item
*/


/* table screensaver_user */

CREATE TABLE screensaver_user (
  username   TEXT PRIMARY KEY,
  password   TEXT NOT NULL
);

COPY screensaver_user (username, password) FROM STDIN;
atolopko	atolopko
jsullivan	jsullivan
\.


/* table screensaver_user_role */

CREATE TABLE screensaver_user_role (
  role_name  TEXT PRIMARY KEY
);

ALTER TABLE screensaver_user_role ADD
  CHECK (role_name IN (
    'Medicinal Chemist',
    'Screensaver Administrator',
    'Screening Room User'
  ));

COPY screensaver_user_role (role_name) FROM STDIN;
Medicinal Chemist
Screensaver Administrator
Screening Room User
\.


/* table screensaver_user_role_link */

CREATE TABLE screensaver_user_role_link (
  username   TEXT NOT NULL REFERENCES screensaver_user,
  role_name  TEXT NOT NULL REFERENCES screensaver_user_role
);

COPY screensaver_user_role_link (username, role_name) FROM STDIN;
jsullivan	Screensaver Administrator
atolopko	Screensaver Administrator
\.


/* table user_classification_taxonomy */

CREATE TABLE user_classification_taxonomy (
  user_classification TEXT PRIMARY KEY
);

COPY user_classification_taxonomy (user_classification) FROM STDIN;
Principal Investigator
Graduate Student
ICCB Fellow
Research Assistant
Postdoc
Other
\.


/* table lab_affiliation_category_taxonomy */

CREATE TABLE lab_affiliation_category_taxonomy (
  lab_affiliation_category TEXT PRIMARY KEY
);

COPY lab_affiliation_category_taxonomy (lab_affiliation_category) FROM STDIN;
HMS
HMS Affiliated Hospital
HSPH
Broad/ICG
Harvard FAS
Non-Harvard
Other
\.


/* table screening_room_user */

CREATE TABLE screening_room_user (
  screening_room_user_id   SERIAL PRIMARY KEY,
  screensaver_username     TEXT REFERENCES screensaver_user,
  date_created             DATE NOT NULL,
  first_name               TEXT NOT NULL,
  last_name                TEXT NOT NULL,
  harvard_id               TEXT,
  phone                    TEXT,
  email                    TEXT NOT NULL,
  lab_head                 INT REFERENCES screening_room_user,
  user_classification      TEXT NOT NULL
                           REFERENCES user_classification_taxonomy,
  lab_affiliation_name     TEXT,
  lab_affiliation_category TEXT REFERENCES lab_affiliation_category_taxonomy,
  is_non_screening_user    BOOLEAN NOT NULL,
  is_rnai_user             BOOLEAN NOT NULL,
  mailing_address          TEXT,
  comments                 TEXT
);


/* table checklist_item_type */

CREATE TABLE checklist_item_type (
  name             TEXT PRIMARY KEY,
  order_statistic  INT NOT NULL UNIQUE,
  has_deactivation BOOLEAN NOT NULL
);

COPY checklist_item_type (name, order_statistic, has_deactivation) FROM STDIN;
'Non HMS Biosafety Training Form on File'	1	1
'ICCB server account set up'	2	1
'ID submitted for access to screening room'	3	1
'Added to ICCB screening users list'	4	1
'Added to PI email list'	5	1
'Added to autoscope users list'	6	1
'Historical ICCB server account requested'	7	1
'Data sharing agreement signed'	8	0
'ID submitted for C-607 access'	9	0
'CellWoRx training'	10	0
'Autoscope training'	11	0
'Image Analysis I training'	12	0
\.


/* table checklist_item */

CREATE TABLE checklist_item (
  screening_room_user_id   INT NOT NULL REFERENCES screening_room_user,
  checklist_item_type_name TEXT NOT NULL REFERENCES checklist_item_type,
  activation_date          DATE,
  activation_initials      TEXT,
  deactivation_date        DATE,
  deactivation_initials    TEXT
);
