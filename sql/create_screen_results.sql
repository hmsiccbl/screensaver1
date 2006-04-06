/* screensaver/sql/create_screen_results.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   screen_result
**   activity_indicator_type_taxonomy
**   indicator_direction_taxonomy
**   result_value_type
**   screen_result_value_order
**   result_value
*/


/* table screen_result */

CREATE TABLE screen_result (
  screen_id          INT NOT NULL REFERENCES screen,
  date_created       DATE NOT NULL,
  replicate_count    INT NOT NULL,
  result_value_count INT NOT NULL,
  is_sharable        BOOLEAN NOT NULL
);


/* table activity_indicator_type_taxonomy */

CREATE TABLE activity_indicator_type_taxonomy (
  activity_indicator_type TEXT PRIMARY KEY
);

COPY activity_indicator_type_taxonomy (activity_indicator_type) FROM STDIN;
Numerical
Boolean
Scaled
\.


/* table indicator_direction_taxonomy */

CREATE TABLE indicator_direction_taxonomy (
  indicator_direction TEXT PRIMARY KEY
);

COPY indicator_direction_taxonomy (indicator_direction) FROM STDIN;
High Values Indicate
Low Values Indicate
\.


/* table result_value_type */

CREATE TABLE result_value_type (
  result_value_type_id    SERIAL PRIMARY KEY,
  date_created            DATE NOT NULL,
  name                    TEXT NOT NULL,
  description             TEXT NOT NULL,
  replicate_number        INT NOT NULL,
  time_point              TEXT NOT NULL,
  is_derived              BOOLEAN NOT NULL,
  how_derived             TEXT,
  is_activity_indicator   BOOLEAN NOT NULL,
  activity_indicator_type TEXT REFERENCES activity_indicator_type_taxonomy,
  indicator_direction     TEXT REFERENCES indicator_direction_taxonomy,
  indicator_cutoff        REAL,
  is_followup_data        BOOLEAN NOT NULL,
  assay_phenotype         TEXT NOT NULL,
  is_cherry_pick          BOOLEAN NOT NULL,
  comments                TEXT NOT NULL
);


/* table screen_result_value_order */

CREATE TABLE screen_result_value_order (
  screen_id            INT NOT NULL REFERENCES screen,
  result_value_type_id INT NOT NULL REFERENCES result_value_type,
  position             INT NOT NULL
);


/* table result_value */

CREATE TABLE result_value (
  result_value_id      SERIAL PRIMARY KEY,
  screen_id            INT NOT NULL REFERENCES screen,
  result_value_type_id INT NOT NULL REFERENCES result_value_type,
  well_id              INT NOT NULL REFERENCES well,
  result_value         TEXT NOT NULL
);
