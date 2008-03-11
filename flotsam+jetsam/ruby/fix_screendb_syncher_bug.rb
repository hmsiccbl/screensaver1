#! /usr/bin/env ruby -w
#
# the ScreenDBSynchronizer has been crazily propagating cherry_pick_liquid_transfers like a cancer! (see the ticket.)
#
# https://rt.med.harvard.edu/Ticket/Display.html?id=113324
#
# my first ever ruby script should fix it! have fun!
#
# this script ran against production on 2008.03.11. it should not need to be run again, unless a new bug appears with
# the exact same effect, or an old (pre svn r2226) version of the screendb syncher is run against prod. 

require 'dbi'
require 'postgres'

# the name of the database follows 'DBI:Pg:'. the next two args are db username and password
dbh = DBI.connect('DBI:Pg:screensaver-fully-loaded-old-rv', 's', '')

# find all the CPLTs for small molecule screens that do not have any associated CPAPs.
# tricky left outer join / is null concept provided by PHP-RATNEST podcast
sth = dbh.prepare('
select cplt.*
from
  screen as s,
  (activity join screening_room_activity     using (activity_id)
            join cherry_pick_liquid_transfer using (activity_id)) as cplt
  left outer join cherry_pick_assay_plate as cpap on cplt.activity_id = cpap.cherry_pick_liquid_transfer_id
where
  s.screen_id = cplt.screen_id and
  s.screen_type = \'Small Molecule\'
  and cpap.cherry_pick_assay_plate_id is null
')
sth.execute

count = 0
while row = sth.fetch_hash do
  count += 1
  activity_id = row['activity_id']
  puts activity_id
  ['cherry_pick_liquid_transfer', 'screening_room_activity', 'activity'].each do |activity_table|
    dbh.do('delete from ' + activity_table + ' where activity_id = ' + activity_id.to_s)
  end
end

sth.finish
dbh.disconnect

puts "total count is #{count}"

