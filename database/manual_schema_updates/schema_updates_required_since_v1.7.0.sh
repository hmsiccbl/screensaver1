# Usage:
# schema_updates_required_since_v1.7.0.sh <db name> <db user> <db host>

cd `dirname $0` && perl schema_updates_required.pl 2679 . "$@"