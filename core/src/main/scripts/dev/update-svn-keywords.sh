# sets the svn property 'svn:keywords' to 'HeadURL Id' for all file
# below the current directory that contain the string '$HeadURL',
# allowing the file header information to be automatically updated by
# subversion when the file is committed
find . -type f -not -path '*/.svn/*' -exec grep -l '\$HeadURL' {} \; | xargs -i core/src/main/scripts/dev/add-svn-keywords.pl {} HeadURL Id
