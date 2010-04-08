# appends a new year to the copyright notice in every file below the current directory that currently contains a copyright notice
# see http://www.gnu.org/prep/maintain/html_node/Copyright-Notices.html
# TODO: update this file as necessary with the previous and current year before running
find . -type f -not -path '*/.svn/*' -exec grep -l 'Copyright � ' {} \; | xargs -i perl -pi -e 's/(Copyright � .*, 2010)/$1, 2011/'  {}
