#! /usr/bin/perl -w
#
# generate a list of the non-empty tables in screendb


### connect to postgres database "screendb"

use DBI;
my $dbh = DBI->connect
    ("dbi:Pg:dbname=screendb", "s", "",
     { RaiseError => 1,
       AutoCommit => 1,
       LongReadLen => 1000000,
       FetchHashKeyName => 'NAME_lc',
       })
    or die "couldnt connect to database";


### prepared statements

my $table_names_sth = $dbh->prepare(<<EOS
SELECT table_name FROM information_schema.tables
WHERE
  table_catalog = 'screendb' AND
  table_schema = 'public'
ORDER BY
  table_name
EOS
);


### execution

$table_names_sth->execute();
while (my ($table_name) = $table_names_sth->fetchrow_array()) {
    my $table_count_sth = $dbh->prepare(<<EOS
SELECT COUNT(*) FROM $table_name
EOS
);
    $table_count_sth->execute();
    my ($table_count) = $table_count_sth->fetchrow_array();
    if ($table_count) {
	print $table_name, "\n";
    }
}

