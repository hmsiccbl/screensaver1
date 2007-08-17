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

  // static fields
  
  private static Pattern plateNumberPattern = Pattern.compile(ScreenResultWorkbookSpecification.WELL_NAME_REGEX);

  
  // instance fields
  
  private ParseErrorManager _errors;
  
  
  // public constructor and instance methods
  
  public WellNameParser(ParseErrorManager errors)
  {
    _errors = errors;
  }
  
  public String parse(Cell cell) 
  {
    String cellString = cell.getString();
    if (cellString == null) {
      _errors.addError("well name cell is empty", cell);
      return "";      
    }
    Matcher matcher = plateNumberPattern.matcher(cellString);
    if (!matcher.matches()) {
      _errors.addError("unparseable well name '" + cellString + "'", cell);
      return "";
    }
    return matcher.group(0);
  }

  public List<String> parseList(Cell cell)
  {
    throw new UnsupportedOperationException();
  }
}