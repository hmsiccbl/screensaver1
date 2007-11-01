# Filters out the COPY command for a single table from the output of
# pg_dump (without the "-i" option!).  This allows a db load to be
# performed more quickly if data for a (large) table is not needed.
# To use, pipe the pg_dump output into this script and specify the
# table name as the sole argument, then pipe this script's output to
# psql for loading.  Chain together to filter multiple
# tables. Defaults to filtering out the
# result_value_type_result_values table.

sed -e "/^COPY ${1:-result_value_type_result_values}/,/^\\\./ d"
