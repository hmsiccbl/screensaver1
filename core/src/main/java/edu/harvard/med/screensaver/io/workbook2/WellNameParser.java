// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultWorkbookSpecification;


/**
 * Parses the value of a cell containing a "well name". Validates that the
 * well name follows proper syntax, defined by the regex "[A-Z]\d\d".
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WellNameParser implements CellValueParser<String>
{
  private static Pattern plateNumberPattern = Pattern.compile(ScreenResultWorkbookSpecification.WELL_NAME_REGEX);

  public WellNameParser()
  {
  }
  
  public String parse(Cell cell) 
  {
    String cellString = cell.getString();
    if (cellString == null) {
      cell.addError("well name cell is empty");
      return "";      
    }
    Matcher matcher = plateNumberPattern.matcher(cellString);
    if (!matcher.matches()) {
      cell.addError("unparseable well name '" + cellString + "'");
      return "";
    }
    return matcher.group(0);
  }

  public List<String> parseList(Cell cell)
  {
    throw new UnsupportedOperationException();
  }
}