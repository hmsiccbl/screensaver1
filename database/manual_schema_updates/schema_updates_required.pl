#!/usr/bin/perl -w

# Find the schema updates scripts that need to be applied to the
# current database by comparing the updates already applied, as
# recorded in the schema_history database table, to the full set of
# update scripts in the database/manual_schema_updates directory.

# update-schema.pl <ss_dir> <db name> <db user> <db host>

my $ss_dir = shift @ARGV;
my $db_name = shift @ARGV;
my $db_user = shift @ARGV;
my $db_host = shift @ARGV || "localhost";
my $min_rev_to_consider = 1500;
my $script_dir = "$ss_dir/database/manual_schema_updates";

my $connect_options = "-h $db_host -d $db_name -U $db_user";
my $psql_cmd = "psql $connect_options";
print STDERR "psql cmd: $psql_cmd\n";

my %revs_applied = ();
for $line (split(/\n/, `ls -1 $script_dir/*sql`)) {
  if ($line =~ /([0-9]+)/) {
    $revs_applied{$1} = 0;
  }
}
#print(join("\n", sort keys %revs_applied));

push @output, split('\n', `$psql_cmd --tuples-only -c 'select screensaver_revision from schema_history'`);
for $line (@output) {
  $line =~ /^\s*([0-9]+)/;
  warn "unknown revision $1 was apparently applied\n" unless exists $revs_applied{$1};
  $revs_applied{$1} = 1 if $1;
}

print "schema migrations needed (>= rev $min_rev_to_consider):\n";
for $rev (sort keys %revs_applied) {
  unless ($revs_applied{$rev} || $rev < $min_rev_to_consider) {
    print "$rev\n";
  }
}
