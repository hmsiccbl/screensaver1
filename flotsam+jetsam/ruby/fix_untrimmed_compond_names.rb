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

main_sth = dbh.prepare('select compound_id, compound_name from compound_compound_name where compound_name like \'% \'')
main_sth.execute

count = 0
while row = main_sth.fetch_hash do
  count += 1
  compound_id = row['compound_id']
  compound_name = row['compound_name']
  puts "found pair #{compound_id} : #{compound_name}";
  
  compound_name =~ /(.*?)\s+/;
  trimmed_compound_name = $1
  
  # this throws bogus warnings "nonstandard use of \\ in a string literal"
  # this is probably a probably with how the DBI library prepares stmts 
  dup_check_sth = dbh.prepare('select count(*) from compound_compound_name where compound_id = ? and compound_name = ?')
  dup_check_sth.execute(compound_id, trimmed_compound_name)
  dup_count = dup_check_sth.fetch[0]
  dup_check_sth.finish
  
  if (dup_count == 0)
    update_sth = dbh.prepare('update compound_compound_name set compound_name = ? where compound_id = ? and compound_name = ?')
    update_sth.execute(trimmed_compound_name, compound_id, compound_name)
    update_sth.finish
  else
    delete_sth = dbh.prepare('delete from compound_compound_name where compound_id = ? and compound_name = ?')
    delete_sth.execute(compound_id, compound_name)
    delete_sth.finish
  end
end

main_sth.finish
dbh.disconnect

puts "total count is #{count}"

