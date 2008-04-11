#! /usr/bin/env ruby -w
#
# parse through all the SD files in the current directory, creating a
# spreadsheet suitable for loading by the NaturalProductsLibraryContentsParser.
#
# that format is described here:
#
# https://wiki.med.harvard.edu/ICCBL/Screensaver/NaturalProductsExcelFileFormat
#
# this script was created to load vendor id information for the NIBR1 library
# into Screensaver. the structures for this library are private.

print "Plate\tWell\tVendor_Id\tICCB_Num\n"

Dir.entries(".").grep(/\.sdf$/).each do |sd_filename|
  File.open(sd_filename) do |sd_file|

    plate = well = vendor_id = iccb_num = nil

    while line = sd_file.gets do
      if line =~ /^>  <Plate>/ then
        plate = sd_file.readline.sub(/\s*$/, "");
      elsif line =~ /^>  <Well>/ then
        well = sd_file.readline.sub(/\s*$/, "");
      elsif line =~ /^>  <Vendor_ID>/ then
        vendor_id = sd_file.readline.sub(/\s*$/, "");
      elsif line =~ /^>  <ICCB_NUM>/ then
        iccb_num = sd_file.readline.sub(/\s*$/, "");
      elsif line =~ /^\$\$\$\$\s*$/ then

        if plate == nil then
          raise "nil plate"
        elsif well == nil then
          raise "nil well"
        elsif vendor_id == nil then
          raise "nil vendor id"
        elsif iccb_num == nil then
          raise "nil iccb num"
        end

        print "#{plate}\t#{well}\t#{vendor_id}\t#{iccb_num}\n"

        plate = well = vendor_id = iccb_num = nil

      end
    end
  end
end
