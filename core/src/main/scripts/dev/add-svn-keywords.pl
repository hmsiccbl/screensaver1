#!/usr/bin/perl

# adds the specified svn keywords to the 'svn:keywords' subversion property of
# the specified file, iff the keyword is not already an svn:keywords property

my $file = shift;
my @keywords = @ARGV;
my $svn_props=`svn pg svn:keywords $file`;
my $needs_update;
for $keyword (@keywords) {
  $needs_update = 1 unless ($svn_props =~ /$keyword/);
}
if ($needs_update) {
  system("svn propset svn:keywords '" . join(' ', @keywords) . "' $file ");
  print "added svn:keywords @keywords to $file\n";
}


