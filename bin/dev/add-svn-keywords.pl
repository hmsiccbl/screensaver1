#!/usr/bin/perl 

# adds the 'HeadURL' and 'Id' svn keywords to the svn properties of
# the specified files, iff HeadURL is not already an svn:keywords
# property

my $keywords="'HeadURL Id'";

my $file = $ARGV[0];
my $svn_props=`svn pg svn:keywords $file`;
my $has_svn_keywords=($svn_props =~ /HeadURL/);
unless ($has_svn_keywords) {
    system("svn propset svn:keywords $keywords $file ");
    print "added svn:keywords $keywords to $file\n";
}


