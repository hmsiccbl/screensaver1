#!/bin/sh
# Use this script to execute LSF tasks (with "sudo -u screensaver [bjobs.sh]", for example)
BSUB=/opt/lsf/7.0/linux2.6-glibc2.3-x86_64/bin/

# Set up the LSF environment
. /opt/lsf/conf/profile.lsf

$BSUB/"$@"
