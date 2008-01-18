#! /usr/bin/perl -w

my %ss_code_to_material_id;

open MATERIALID_SSCODE, "icbg_materialid_sscode.csv"
    or die "couldnt open icbg_materialid_sscode.csv: $!";
while (<MATERIALID_SSCODE>) {
    chop;
    my ($material_id, $ss_code) = split "\t";
    if ($material_id eq "MATERIAL_ID" && $ss_code eq "SS_CODE") {
	next;
    }
    if ($ss_code_to_material_id{$ss_code}) {
	die "duplicate ss_code in first file: $ss_code";
    }
    $ss_code_to_material_id{$ss_code} = $material_id;
}
close MATERIALID_SSCODE;

open PLATE_WELL_SSCODE, "icbg5_plate_well_sscode.csv"
    or die "couldnt open icbg5_plate_well_sscode.csv: $!";
while (<PLATE_WELL_SSCODE>) {
    chop;
    my ($plate, $well, $ss_code) = split "\t";
    if ($plate eq "PLATE" && $well eq "WELL" && $ss_code eq "SS_CODE") {
	next;
    }
    my $material_id = $ss_code_to_material_id{$ss_code};
    unless ($material_id) {
	die "ss_code in second file but not in first file: $ss_code";
    }
    print "$plate\t$well\t$ss_code\t$material_id\n";
}
close PLATE_WELL_SSCODE;
