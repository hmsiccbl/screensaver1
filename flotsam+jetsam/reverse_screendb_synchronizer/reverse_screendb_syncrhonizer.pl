#! /usr/bin/perl -w
#
# copy RNAi Cherry Pick Requests that were created in Screensaver over to ScreenDB.
#
# - RNAi Cherry Pick Requests that were created in Screensaver have null
#   legacy_cherry_pick_request_number
# 


# open database connections to both screendb and screensaver

use DBI;

my $screendb_dbh = DBI->connect
    ("dbi:Pg:dbname=screendb", "js163", "dugtedent3",
     { RaiseError => 1,
       AutoCommit => 0,
       LongReadLen => 1000000,
       FetchHashKeyName => 'NAME_lc',
       })
    or die "couldnt connect to database";

$screendb_dbh->do("set datestyle to SQL, MDY");

my $screensaver_dbh = DBI->connect
    ("dbi:Pg:dbname=screensaver", "js163", "dugtedent3",
     { RaiseError => 1,
       AutoCommit => 1,
       LongReadLen => 1000000,
       FetchHashKeyName => 'NAME_lc',
       })
    or die "couldnt connect to database";


# prepared statements

my $select_screensaver_cherry_pick_assay_plates_sql = "
SELECT COUNT(*) FROM cherry_pick_assay_plate WHERE cherry_pick_request_id = ?
";
my $select_screensaver_cherry_pick_assay_plates_sth =
    $screensaver_dbh->prepare($select_screensaver_cherry_pick_assay_plates_sql);

my $delete_screendb_rnai_cherry_pick_request_sql = "
DELETE FROM visits WHERE id = ?
";
my $delete_screendb_rnai_cherry_pick_request_sth =
    $screendb_dbh->prepare($delete_screendb_rnai_cherry_pick_request_sql);

my $insert_screendb_rnai_cherry_pick_request_sql = "
INSERT INTO visits (
  id,
  screen_id,
  performed_by,
  date_created,
  visit_type,
  no_replicate_screen,
  vol_of_compd_transf,
  est_final_screen_conc,
  assay_protocol,
  comments,
  cherry_pick_request_date,
  cherry_pick_filenames,
  cherry_pick_volume_per_well
)
VALUES (
  ?,
  ?,
  (SELECT id FROM users WHERE first = ? and last = ?),
  ?,
  'RNAi Cherry Pick',
  0,
  0,
  0,
  ?,
  ?,
  ?,
  ?,
  ?
)
";
my $insert_screendb_rnai_cherry_pick_request_sth =
    $screendb_dbh->prepare($insert_screendb_rnai_cherry_pick_request_sql);


# get all the plate ranges for all the visits for the screen

my $screensaver_created_rnai_cherry_pick_requests_sql = "
SELECT
  cherry_pick_request.*,
  rnai_cherry_pick_request.assay_protocol,
  screen.screen_number,
  screensaver_user.first_name,
  screensaver_user.last_name
FROM
  cherry_pick_request JOIN rnai_cherry_pick_request USING (cherry_pick_request_id),
  screen,
  screensaver_user
WHERE
  legacy_cherry_pick_request_number is null
AND
  cherry_pick_request.screen_id = screen.screen_id
AND
  cherry_pick_request.requested_by_id = screensaver_user.screensaver_user_id
ORDER BY
  cherry_pick_request_id
";

my $screensaver_created_rnai_cherry_pick_requests_sth =
    $screensaver_dbh->prepare($screensaver_created_rnai_cherry_pick_requests_sql);
$screensaver_created_rnai_cherry_pick_requests_sth->execute();


# global counts

my $total_delete_count = 0;
my $total_insert_count = 0;


# iterate over all the screensaver-created rnai cherry pick requests
while (my $screensaver_created_rnai_cherry_pick_request =
       $screensaver_created_rnai_cherry_pick_requests_sth->fetchrow_hashref()) {

    # some mild data cleanup

    $screensaver_created_rnai_cherry_pick_request->{date_requested} =~
        s/(\S+).*/$1/;


    # print some debug info about the request

    print "\nscreensaver-created rnai cherry pick request:\n";
    for my $column (sort keys %{ $screensaver_created_rnai_cherry_pick_request }) {
        my $value = $screensaver_created_rnai_cherry_pick_request->{$column};
        $value = "null" if not defined $value;
        print "  $column\t$value\n";
    }


    # get the id

    my $rnai_cherry_pick_request_id =
        $screensaver_created_rnai_cherry_pick_request->{cherry_pick_request_id};
    print "\n  id = $rnai_cherry_pick_request_id\n";


    # delete any old request for this id

    my $delete_count =
        $delete_screendb_rnai_cherry_pick_request_sth->execute
        ($rnai_cherry_pick_request_id);
    $delete_screendb_rnai_cherry_pick_request_sth->finish();
    $total_delete_count += $delete_count;

    print "  delete count = $delete_count\n";


    # build the cherry_pick_filenames

    my @cherry_pick_filenames = ();
    $select_screensaver_cherry_pick_assay_plates_sth->execute
        ($rnai_cherry_pick_request_id);
    my ($num_plates) =
        $select_screensaver_cherry_pick_assay_plates_sth->fetchrow_array();
    for (my $i = 1; $i <= $num_plates; $i ++) {
        my $plate_ordinal = $i;
        my $cherry_pick_filename =
            $screensaver_created_rnai_cherry_pick_request->{first_name} .
            " " .
            $screensaver_created_rnai_cherry_pick_request->{last_name} .
            " (" .
            $screensaver_created_rnai_cherry_pick_request->{screen_number} .
            ") CP" .
            $rnai_cherry_pick_request_id .
            "  Plate " .
            $plate_ordinal .
            " of " .
            $num_plates;
        push @cherry_pick_filenames, $cherry_pick_filename;
    }
    $select_screensaver_cherry_pick_assay_plates_sth->finish();
    my $cherry_pick_filenames = join ",\n", @cherry_pick_filenames;
    print "cherry_pick_filenames = $cherry_pick_filenames\n";

    # insert the new request

    my $insert_count =
        $insert_screendb_rnai_cherry_pick_request_sth->execute
        (
         $rnai_cherry_pick_request_id,
         $screensaver_created_rnai_cherry_pick_request->{screen_number},
         $screensaver_created_rnai_cherry_pick_request->{first_name},
         $screensaver_created_rnai_cherry_pick_request->{last_name},
         '2007-08-10', # date created??
         $screensaver_created_rnai_cherry_pick_request->{assay_protocol},
         $screensaver_created_rnai_cherry_pick_request->{comments},
         $screensaver_created_rnai_cherry_pick_request->{date_requested},
         $cherry_pick_filenames,
         $screensaver_created_rnai_cherry_pick_request->{microliter_transfer_volume_per_well_approved},
         );
    $insert_screendb_rnai_cherry_pick_request_sth->finish();
    $total_insert_count += $insert_count;

    print "  insert count = $insert_count\n";
}


# disconnect the database handles

$screensaver_dbh->disconnect();
$screendb_dbh->commit();
$screendb_dbh->disconnect();


# print the totals

print "\n\n";
print "Total Delete Count: ";
print "$total_delete_count\n";
print "Total Insert Count: ";
print "$total_insert_count\n";
