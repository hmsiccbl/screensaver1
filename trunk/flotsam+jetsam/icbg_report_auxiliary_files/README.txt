The ICBGReportGenerator depends on the "ICCB-L Plate-Well to INBio LQ Mapping.xls" file to
translate between our Plate-Well and the NAPIS MATERIAL_ID. This file is generated as follows:

1. From our data files for the ICBG libraries, we have a mapping from Plate-Well to ss_code.
(The ss_codes are also stored in the database as vendor_identifier.)

2. InBio provides a mapping from ss_code to MATERIAL_ID. (If they deliver multiple ss_code files,
the one you want to look at is for EXTRACTION.)

3. We glom this info to generate spreadsheet with columns Plate,Well,ss_code,MATERIAL_ID.

This file does not need to be regenerated unless the InBio ss_code to MATERIAL_ID mapping
changes (highly unlikely) or we are screening new ICBG libraries whose plates are not already
mapped.
