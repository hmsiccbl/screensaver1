# Usage:
# schema_updates_required_since_v2010.10.29.sh <db name> <db user> <db host>

cd `dirname $0` && perl schema_updates_required.pl --from-revision 4406 --scripts-dir . --db-name $1 --db-user $2 ${3:+--db-host $3}
