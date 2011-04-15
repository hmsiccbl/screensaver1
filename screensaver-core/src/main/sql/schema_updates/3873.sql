BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
3873,
current_timestamp,
'remove "Numerical" positive indicator type from result_value_type';

/* TODO: this script converts Numerical Positive Indicator result
value types (aka data headers/columns) to a basic numeric data type.
At ICCB-L all screen results that used Numerical Positive Indicator
also had additional positives data columns that were derived from
these columns, and so positives determinations were not lost.  If your
database has screen results that relied exclusvely on Numerical
Positive Indicator data columns for positives determinations, then you
should create a new positives data column to record the positives.  */

update result_value_type set is_positive_indicator = false, is_numeric = true, positive_indicator_type = null where positive_indicator_type = 'Numerical';

alter table result_value_type drop column positive_indicator_cutoff;
alter table result_value_type drop column positive_indicator_direction;

COMMIT;
