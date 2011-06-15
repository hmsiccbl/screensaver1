#!/bin/sh

REL_BRANCH=$1
shift
OPTS=$@
svn merge $OPTS http://forge.abcd.harvard.edu/svn/screensaver/branches/${REL_BRANCH} .