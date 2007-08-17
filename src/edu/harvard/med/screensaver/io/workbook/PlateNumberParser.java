// $HeadURL:$
// $Id:$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultWorkbookSpecification;

/**
 * Parses the value of a cell containing a "plate number". Converts from a
 * "PL-####" format to an <code>Integer</code>.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class PlateNumberParser implements CellValueParser<Integer>
{
  
  // static fields
  
  private static Pattern plateNumberPattern = Pattern.compile(ScreenResultWorkbookSpecification.PLATE_NUMBER_REGEX);
  
  
  // private instance fields
  
  private ParseErrorManager _errors;

  
  // public constructor and instance methods
  
  public PlateNumberParser(ParseErrorManager errors)
  {
    _errors = errors;
  }
  
  public Integer parse(Cell cell) 
  {
    Matcher matcher = plateNumberPattern.matcher(cell.getAsString());
    if (!matcher.matches()) {
      _errors.addError("unparseable plate number '" + cell.getAsString(false) + "'",
                       cell);
      return -1;
    }
    return new Integer(matcher.group(2));
  }

  public List<Integer> parseList(Cell cell)
  {
    throw new UnsupportedOperationException();
  }
}