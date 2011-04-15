// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.WellKey;

public class PlateWellListParser
{
  private static Logger log = Logger.getLogger(PlateWellListParser.class);

  // TODO: consider moving these to WellKey
  private static final Pattern _plateWellHeaderLinePattern = Pattern.compile(
    "^\\s*((((stock|384)\\s+)?plate)\\s+(((stock|384)\\s+)?well)|(stock_id\\s+row))\\s*$",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern _plateWellPattern = Pattern.compile(
    "^\\s*((PL[-_]?)?(\\d+))([:;,])?[A-P](0?[1-9]|1[0-9]|2[0-4]).*",
    Pattern.CASE_INSENSITIVE);

  public PlateWellListParser()
  {}

  /**
   * Parse and return the list of well keys from the plate-well list.
   * @param plateWellList the plate-well list
   * @throws IOException
   * @return the list of wells
   */
  public static PlateWellListParserResult parseWellsFromPlateWellList(String plateWellList)
  {
    PlateWellListParserResult result = new PlateWellListParserResult();
    BufferedReader plateWellListReader = new BufferedReader(new StringReader(plateWellList));
    try {
      int lineNumber = 0;
      for (
        String line = plateWellListReader.readLine();
        line != null;
        line = plateWellListReader.readLine()) {

        ++lineNumber;

        // skip lines that say "Plate Well"
        Matcher matcher = _plateWellHeaderLinePattern.matcher(line);
        if (matcher.matches()) {
          continue;
        }

        // trim leading and trailing whitespace, and skip blank lines
        line = line.trim();
        if (line.equals("")) {
          continue;
        }

        // separate initial plate and well with a space if necessary
        line = splitInitialPlateWell(line);

        // split the line into tokens; should be one plate, then one or more wells
        String [] tokens = line.split("[\\s;,]+");
        if (tokens.length == 0) {
          continue;
        }

        Integer plateNumber;
        try {
          plateNumber = Integer.valueOf(tokens[0]);
        }
        catch (NumberFormatException e) {
          result.addError(lineNumber, "invalid plate number " + tokens[0]);
          continue;
        }
        for (int i = 1; i < tokens.length; i ++) {
          try {
            result.addParsedWellKey(new WellKey(plateNumber, tokens[i]));
          }
          catch (IllegalArgumentException e) {
            result.addError(lineNumber, "invalid well name " + tokens[i] + " (plate " + plateNumber + ")");
            continue;
          }
        }
      }
    }
    catch (IOException e) {
      result.addError(0, "internal error: could not read plateWellList");
    }
    return result;
  }

  /**
   * Insert a space between the first plate number and well name if there is no
   * space there already.
   * @param line the line to patch up
   * @return the patched up line
   */
  private static String splitInitialPlateWell(String line)
  {
    Matcher matcher = _plateWellPattern.matcher(line);
    if (matcher.matches()) {
      int spliceStartIndex = matcher.group(1).length();
      int spliceEndIndex = spliceStartIndex;
      if (matcher.group(4) != null) {
        spliceEndIndex ++;
      }
      line = line.substring(0, spliceStartIndex) + " " + line.substring(spliceEndIndex);
      if (log.isDebugEnabled()) {
        log.debug("spliced line = " + line);
      }
    }
    return line;
  }
}

