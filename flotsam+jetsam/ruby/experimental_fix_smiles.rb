#! /usr/bin/env ruby -w
#
# i tried this script out, then rerunning the ChembankIdUpgrader. it didnt help any :(

require 'dbi'
require 'postgres'

# the name of the database follows 'DBI:Pg:'. the next two args are db username and password
dbh = DBI.connect('DBI:Pg:screensaver-fully-loaded', 's', '')

main_sth = dbh.prepare('select compound_id, smiles from compound where smiles like \'%1%1%1%1\'')
main_sth.execute

count = 0
while row = main_sth.fetch_hash do
  count += 1
  compound_id = row['compound_id']
  smiles = row['smiles']

  fresh_int = 2
  while smiles =~ /#{fresh_int}/ do
    fresh_int += 1
  end

  # puts "old smiles is #{smiles}"
  
  regexp = Regexp.new(/1.*?1.*?(1).*?(1)/)
  while true
    match = regexp.match(smiles)
    break if match == nil
    third_one_pos = match.offset(1)[0]
    fourth_one_pos = match.offset(2)[0]

    #puts "third and fourth one pos are #{third_one_pos} and #{fourth_one_pos}"

    smiles[third_one_pos] = "#{fresh_int}"
    smiles[fourth_one_pos] = "#{fresh_int}"

    fresh_int += 1

    #puts "new smiles is #{smiles}"
  end

  update_sth = dbh.prepare('update compound set smiles = ? where compound_id = ?')
  update_sth.execute(smiles, compound_id)
  update_sth.finish
end

main_sth.finish
dbh.disconnect

puts "total count is #{count}"

