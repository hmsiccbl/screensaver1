This directory contains manually generated SQL files needed to be run to bring existing schemas
and (populated) databases up-to-date with the latest Hibernate model. These files are named after
the SVN revision number they apply to. The expectation here is that these SQL files will be run
by hand, when necessary, to bring an existing database up-to-date with the latest codebase, so that
we can deploy the latest codebase against it.

These SQL files have only started being maintained at svn revision 1109. This should be sufficient
for us, since there are no extant databases in need of revisions to bring up-to-date further than
that. The oldest extant database was probably created a number of revisions back from that, but 1109
is most likely the first svn revision with schema changes of any significance.

WARNING: make sure you run these database updates as the appropriate user! (Normally the web user.)
Otherwise you will run into permissions problems.
