package edu.harvard.med.screensaver.io.workbook;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.io.screenresult.ScreenResultWorkbookSpecification;

/**
 * Parses the value of a cell containing a "plate number". Converts from a
 * "PL-####" format to an <code>Integer</code>.
 * 
 * @author ant
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
    Matcher matcher = plateNumberPattern.matcher(cell.getString());
    if (!matcher.matches()) {
      _errors.addError("unparseable plate number '" + cell.getString() + "'",
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