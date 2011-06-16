#!/bin/sh

DEVELOPER=$1
TICKET=$2
shift 2
OPTS=$@
NEW_BRANCH=http://forge.abcd.harvard.edu/svn/screensaver/branches/$DEVELOPER/$TICKET
svn copy -m "Development branch for ticket [#$TICKET]" $OPTS http://forge.abcd.harvard.edu/svn/screensaver/trunk $NEW_BRANCH
echo created $NEW_BRANCH
