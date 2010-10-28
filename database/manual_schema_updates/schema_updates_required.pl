#!/usr/bin/perl -w

# Find the schema migration updates scripts that need to be applied to the
# specified database.  This set of required scripts is determined by 
# comparing the updates already applied, as recorded in the schema_history 
# database table, to the set of update scripts in the specified directory.

# Usage:
# schema_updates_required.pl --scripts-dir <migration scripts dir> --db-name <db name> --db-user <db user> [--db-host <db host>] --from-revision <from revision> [--revision <revision>]

use File::Find;
use Getopt::Long;

my $migration_scripts_dir = "database/manual_migration/scripts";
my $db_name;
my $db_user;
my $db_host = "localhost";
my @revisions;
my $from_revision;

GetOptions("scripts-directory|sd=s" => \$migration_scripts_dir,
           "db-name|D=s" => \$db_name,
           "db-user|U=s" => \$db_user,
           "db-host|H=s" => \$db_host,
           "revision=i" => \@revisions,
           "from-revision=i" => \$from_revision);


my %required_scripts = ();
my $connect_options = "-h $db_host -d $db_name -U $db_user";
my $psql_cmd = "psql $connect_options";
print STDERR "psql cmd: $psql_cmd\n";

sub process { if (/^([0-9]+)\.sql$/) { if ($1 >= $from_revision || grep(/^$1$/, @revisions)) { $required_scripts{$1} = 0 } } }
find({ wanted => \&process,  }, $migration_scripts_dir);
#print(join("\n", sort keys %required_scripts));

push @output, split('\n', `$psql_cmd --tuples-only -c 'select screensaver_revision from schema_history'`);
for $line (@output) {
  $line =~ /^\s*([0-9]+)/;
  $rev_applied = $1;
 if ($rev_applied >= $from_revision and !exists $required_scripts{$rev_applied}) {
   warn "migration script for revision $rev_applied was apparently applied, but migration script does not exist\n";
 }
  $required_scripts{$1} = 1 if $1;
}

print STDERR "schema migrations required (>= rev $from_revision):\n";
for $rev (sort keys %required_scripts) {
  print "$rev\n"  unless $required_scripts{$rev};
}
