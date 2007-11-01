#! /usr/bin/perl -w
#
# import the buffer and DMSO well types from a file. the file to load is named
# List_of_DMSO_or_Buffer_wells_for_JS_053107.xls and is available on the Wiki at
# the bottom of the https://wiki.med.harvard.edu/ICCBL/LibraryDocumentation
# page.
#
# database connection info is hardcoded into the script. the checked in connection
# info is relatively benign. if you edit it to check in to a database that should
# remain secure (such as the production or development databases on orchestra),
# be sure not to check in those changes!
#
# some notes:
#   - the column naming and column ordering is fixed
#   - every sheet is expected to have the same format - no blank sheets eh!


use DBI;

my $screensaver_dbh = DBI->connect
    ("dbi:Pg:dbname=screensaver", "s", "",
     { RaiseError => 1,
       AutoCommit => 1,
       LongReadLen => 1000000,
       FetchHashKeyName => 'NAME_lc',
   })
    or die "couldnt connect to database";

my $screensaver_sth = $screensaver_dbh->prepare
    ("UPDATE well SET well_type = ? WHERE well_id = ?");

use Spreadsheet::ParseExcel;

my $excel_filename = "List_of_DMSO_or_Buffer_wells_for_JS_053107.xls";
my $excel_file = Spreadsheet::ParseExcel::Workbook->Parse($excel_filename);
my @sheets = @{ $excel_file->{Worksheet} };

for my $sheet (@sheets) {
    check_sheet_headers($sheet);
    parse_sheet_rows($sheet);
}

sub check_sheet_headers {
    my $sheet = shift;
    my $sheet_name = $sheet->{Name};
    $sheet->{Cells}[0][0]->{Val} eq "Plate #" or die
	"expected column header \"Plate #\" in A column on sheet \"" .
	$sheet_name . "\" (got \"" . $sheet->{Cells}[0][0]->{Val} . "\")\n";
    $sheet->{Cells}[0][1]->{Val} eq "Column" or die
	"expected column header \"Column\" in B column on sheet \"" .
	$sheet_name . "\" (got \"" . $sheet->{Cells}[0][1]->{Val} . "\"\n";
    $sheet->{Cells}[0][2]->{Val} eq "Row" or die
	"expected column header \"Row\" in C column on sheet \"" .
	$sheet_name . "\" (got \"" . $sheet->{Cells}[0][2]->{Val} . "\")\n";
    $sheet->{Cells}[0][3]->{Val} eq "Well Type" or die
	"expected column header \"Well Type\" in D column on sheet \"" .
	$sheet_name . "\" (got \"" . $sheet->{Cells}[0][3]->{Val} . "\")\n";
}

sub parse_sheet_rows {
    my $sheet = shift;
    for (my $i = 1; $i <= $sheet->{MaxRow}; $i ++) {
        my $plates = $sheet->{Cells}[$i][0]->{Val};
        my $columns = $sheet->{Cells}[$i][1]->{Val};
        my $rows = $sheet->{Cells}[$i][2]->{Val};
        my $well_type = $sheet->{Cells}[$i][3]->{Val};

	# remove commas in rnai plates
	$plates =~ s/50,/50/g;

	if ($well_type =~ /dmso/i) {
	    $well_type = "DMSO";
	}
	elsif ($well_type =~ /buffer/i) {
	    $well_type = "buffer";
	}

	for my $plate (parse_plate_or_column($plates)) {
	    #next if $plate =~ /^5009[12]$/;
	    for my $column (parse_plate_or_column($columns)) {
		for my $row (parse_rows($rows)) {
		    my $well_id = sprintf
			"%05d:%s%02d", $plate, $row, $column;
		    update_well_type($well_id, $well_type);
		}
	    }
	}
    }
}

sub parse_plate_or_column {
    my $plate_or_column = shift;
    $plate_or_column =~ /^[\d\s,-]+$/ or die
	"bad plate or column $plate_or_column\n";
    my @expanded = ();
    while ($plate_or_column =~ /\G([,-])?\s*(\d+)/g) {
	my $op = $1;
	my $number = $2;
	if ($op and $op eq "-") {
	    $prev_number = pop @expanded;
	    defined $prev_number or die "- without preceding number\n";
	    push @expanded, ($prev_number .. $number);
	}
	else {
	    push @expanded, $number;
	}
    }
    @expanded;
}

sub parse_rows {
    my $rows = shift;
    $rows =~ /^[\sA-P,-]+$/ or die "bad row $rows\n";
    my @expanded = ();
    while ($rows =~ /\G([,-])?\s*([A-P])/g) {
	my $op = $1;
	my $letter = $2;
	if ($op and $op eq "-") {
	    $prev_letter = pop @expanded;
	    defined $prev_letter or die "- without preceding letter\n";
	    push @expanded, ($prev_letter .. $letter);
	}
	else {
	    push @expanded, $letter;
	}
    }
    @expanded;
}

sub update_well_type {
    my $well_id = shift;
    my $well_type = shift;
    my $count = $screensaver_sth->execute($well_type, $well_id);
    $count == 1 or die "problem updating $well_id to $well_type\n";
}
