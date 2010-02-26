#!/usr/bin/perl -w

# Find the schema migration updates scripts that need to be applied to the
# specified database.  This set of required scripts is determined by 
# comparing the updates already applied, as recorded in the schema_history 
# database table, to the set of update scripts in the specified directory.

# Usage:
# schema_updates_required.pl <from revision> <migration scripts dir> <db name> <db user> <db host>

use File::Find;

my $from_revision = shift @ARGV;
my $migration_scripts_dir = shift @ARGV;
my $db_name = shift @ARGV;
my $db_user = shift @ARGV;
my $db_host = shift @ARGV || "localhost";

my $connect_options = "-h $db_host -d $db_name -U $db_user";
my $psql_cmd = "psql $connect_options";
print STDERR "psql cmd: $psql_cmd\n";

my %revisions = ();

sub process { if (/^([0-9]+)\.sql$/) { if ($1 >= $from_revision) { $revisions{$1} = 0 } } }
find({ wanted => \&process,  }, $migration_scripts_dir);
#print(join("\n", sort keys %revisions));

push @output, split('\n', `$psql_cmd --tuples-only -c 'select screensaver_revision from schema_history'`);
for $line (@output) {
  $line =~ /^\s*([0-9]+)/;
  $rev_applied = $1;
 if ($rev_applied >= $from_revision and !exists $revisions{$rev_applied}) {
   warn "migration script for revision $rev_applied was apparently applied, but migration script does not exist\n";
 }
  $revisions{$1} = 1 if $1;
}

print STDERR "schema migrations required (>= rev $from_revision):\n";
for $rev (sort keys %revisions) {
  print "$rev\n"  unless $revisions{$rev};
}
