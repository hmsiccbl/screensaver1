/* screensaver/sql/create_derivatives.sql
** by john sullivan, 2006.04
**
** creates the following tables:
**   derivative
**   derivative_synonym
**   derivative_screen_result
*/


/* table derivative */

CREATE TABLE derivative (
  derivative_id SERIAL PRIMARY KEY,
  name          TEXT NOT NULL,
  smiles        TEXT NOT NULL
);


/* table derivative_synonym */

CREATE TABLE derivative_synonym (
  derivative_id INT NOT NULL REFERENCES derivative,
  synonym       TEXT NOT NULL
);


/* table derivative_screen_result */

CREATE TABLE derivative_screen_result (
  derivative_id  INT NOT NULL REFERENCES derivative,
  activity_level TEXT NOT NULL,
  activity_type  TEXT NOT NULL
);
