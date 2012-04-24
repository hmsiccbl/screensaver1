# Usage:
# schema_updates_required_since_v1.7.4.sh <db name> <db user> <db host>
expectedNumberArgs=2

if [ $# -lt $expectedNumberArgs ]
then
  echo "Usage: `basename $0` $expectedNumberArgs args expected"
  echo "schema_updates_required_since_v1.7.4.sh <db name> <db user> <db host>"
  echo "args: $#"
  exit $WRONG_ARGS
fi

cd `dirname $0` && perl schema_updates_required.pl --from-revision 2747 --scripts-dir . --db-name $1 --db-user $2 ${3:+--db-host $3}
