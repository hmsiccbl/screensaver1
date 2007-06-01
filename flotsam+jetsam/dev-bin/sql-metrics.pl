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

my $state = "in_request";
my $request_url;
my $start_time;
my $end_time;
my $user_activity;
my @sql_stmts;

while (<>) {
  chomp;
  if (/^(\d+:\d+:\d+,\d+).*>>>>.* @ (\S+)/) {
    init();
    $start_time = $1;
    $request_url = $2;
    $state = "processing_request";
  }
  elsif ($state eq "processing_request" && /userActivity.*\) (.*)/) {
    $user_activity = $1;
  }
  elsif ($state eq "processing_request" && /org\.hibernate\.SQL:\d+ - (.*)/) {
    push @sql_stmts, $1 if $1;
  }
  elsif ($state eq "processing_request" && /^(\d+:\d+:\d+,\d+).*<<<</) {
    $end_time = $1;
    report();
    $state = "awaiting_request";
  }
}

sub init {
  $request_url = undef;
  $user_activity = undef;
  @sql_stmts = ();
  $start_time = undef;
  $end_time = undef;
}

sub report {
  my $activity = $request_url;
  $activity .= " (" . $user_activity . ")" if $user_activity;
  print "\n$activity took " . elapsed_seconds($start_time, $end_time) . "\n";
  print "\tSQL statements: " . scalar(@sql_stmts) . "\n";
  my $n = 1;
  foreach my $sql (@sql_stmts) {
    #print "\t\tsql: $sql\n";
    print "\t#$n: ";
    $sql =~ / from (.*)/;
    my $from_clause = $1;
    my %tables;
    my $table_order = 0;
    foreach my $join_clause (split(/ join /, $from_clause)) {
      $join_clause =~ /^(\w+)/;
      my $table = $1;
      $tables{$table} = $table_order unless exists $tables{$table};
      ++$table_order;
    }
    my @sorted_tables = sort { $tables{$a} <=> $tables{$b} } keys %tables;
    print join(", ", @sorted_tables), "\n";
    ++$n;
  }
}

sub elapsed_seconds {
  my ($start_time, $end_time) = @_;
  $start_time =~ /([0-9]+):(\d+):(\d+),(\d+)/;
  my $start_time_ms = ($1 * 60*60*1000) + ($2 * 60*1000) + ($3 * 1000) + $4;
  $end_time =~ /(\d+):(\d+):(\d+),(\d+)/;
  my $end_time_ms = ($1 * 60*60*1000) + ($2 * 60*1000) + ($3 * 1000) + $4;
  my $elapsed_ms = $end_time_ms - $start_time_ms;
  return $elapsed_ms / 1000;
}
