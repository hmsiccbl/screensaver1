#!/usr/bin/perl -w

# Outputs metrics for SQL statements in screensaver.log.  The following log4j properties should be configured as follows:
#
# log4j.logger.edu.harvard.med.screensaver.ui.util.ScreensaverServletFilter=debug
# log4j.logger.org.hibernate.SQL=debug
#
# typical usage:
# $ tail -f /usr/local/tomcat/logs/screensaver.log | perl sql-metrics.pl

# Bugs:
# - does not handle interleaved request procesing (insufficient information in log)
# - does not properly calc elapsed time on requests that cross midnight

use strict;

$| = 1;

my $state = "in_request";
my $request_url;
my $activity;
my $time_stamp;
my @ordered_activities;
my %start_time_by_activity;
my %end_time_by_activity;
my %sql_stmts_by_activity;

while (<>) {
  chomp;
  if (/^\S+\s(\d+:\d+:\d+,\d+)/) {
    $time_stamp = $1;
  }
  if (/>>>>.* @ (\S+)/) {
    init();
    $start_time_by_activity{$activity} = $time_stamp;
    $request_url = $1;
    $state = "processing_request";
  }
  elsif ($state eq "processing_request" && /userActivity.*\) (.*)/) {
    $end_time_by_activity{$activity} = $time_stamp;
    $activity = $1;
    $start_time_by_activity{$activity} = $time_stamp;
  }
  elsif ($state eq "processing_request" && /org\.hibernate\.SQL:\d+ - (.*)/) {
    if (!exists $sql_stmts_by_activity{$activity}) {
      push @ordered_activities, $activity;
    }
    push @{$sql_stmts_by_activity{$activity}}, $1 if $1;
  }
  elsif ($state eq "processing_request" && /<<<</) {
    $end_time_by_activity{$activity} = $time_stamp;
    report();
    $state = "awaiting_request";
  }
}

sub init {
  $request_url = undef;
  $activity = "system activity";
  @ordered_activities = ();
  %sql_stmts_by_activity = ();
  %start_time_by_activity = ();
  %end_time_by_activity = ();
}

sub report {
  return unless @ordered_activities;

  print "\n", '=' x 80, "\n";
  foreach my $activity (@ordered_activities) {
    my $activity_desc = $activity . " [" . $request_url . "]";
    print '-' x 80, "\n";
    print elapsed_seconds($start_time_by_activity{$activity}, $end_time_by_activity{$activity}) . "s: $activity_desc\n";
    my @sql_stmts = @{$sql_stmts_by_activity{$activity}};
    print "\tSQL statements: " . scalar(@sql_stmts) . "\n";
    my $n = 1;
    foreach my $sql (@sql_stmts) {
      print "\t#$n: ";
      my $sql_op = ($sql =~ /^(\w+)/)[0];
      my $from_clause = ($sql =~ / from (.*)( where )?/)[0];
      my $where_clause = ($sql =~ / where (.*)/)[0];
      #my $primary_table_alias = ($where_clause =~ /(.*?)\..*?=\?/)[0];

      my %tables;
      my $table_order = 0;
      #my %alias_to_table;
      if ($sql_op eq "select" && $from_clause) {
        foreach my $join_clause (split(/ join /, $from_clause)) {
          $join_clause =~ /^(\w+) (\w+)/;
          my $table = $1;
          #my $table_alias = $2;
          #$alias_to_table{$table_alias} = $table;
          $tables{$table} = $table_order unless exists $tables{$table};
          ++$table_order;
        }
      }

      #my $primary_table = $primary_table_alias ? $alias_to_table{$primary_table_alias} : "";
      my @sorted_tables;
      if ($sql_op eq "select") {
        @sorted_tables = sort { $tables{$a} <=> $tables{$b} } keys %tables;
      }
      elsif (!($sql_op eq "select")) {
        push @sorted_tables, ($sql =~ /^\w+ (into |from )?(\S+)/)[1];
      }
      #map {$_ . ($_ eq $primary_table ? "(*)" : "")}*/
      print "[$sql_op] ";
      print join(", ", @sorted_tables);
      print " (NO WHERE CLAUSE!)" if $sql_op eq "select" && !$where_clause;
      print "\n";

      ++$n;
    }
  }
}

sub elapsed_seconds {
  my ($start_time, $end_time) = @_;
  $start_time =~ /(\d+):(\d+):(\d+),(\d+)/;
  my $start_time_ms = ($1 * 60*60*1000) + ($2 * 60*1000) + ($3 * 1000) + $4;
  $end_time =~ /(\d+):(\d+):(\d+),(\d+)/;
  my $end_time_ms = ($1 * 60*60*1000) + ($2 * 60*1000) + ($3 * 1000) + $4;
  my $elapsed_ms = $end_time_ms - $start_time_ms;
  return $elapsed_ms / 1000;
}
