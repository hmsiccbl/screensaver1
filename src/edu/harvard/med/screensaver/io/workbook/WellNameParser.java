package edu.harvard.med.screensaver.io.workbook;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses the value of a cell containing a "well name". Validates that the
 * well name follows proper syntax, defined by the regex "[A-Z]\d\d".
 * 
 * @author ant
 */
public class WellNameParser implements CellValueParser<String>
{

  // static fields
  
  private static Pattern plateNumberPattern = Pattern.compile("[A-P]\\d\\d");

  
  // instance fields
  
  private ParseErrorManager _errors;
  
  
  // public constructor and instance methods
  
  public WellNameParser(ParseErrorManager errors)
  {
    _errors = errors;
  }
  
  public String parse(Cell cell) 
  {
    Matcher matcher = plateNumberPattern.matcher(cell.getString());
    if (!matcher.matches()) {
      _errors.addError("unparseable well name '" + cell.getString() + "'",
                       cell);
      return "";
    }
    return matcher.group(0);
  }

  public List<String> parseList(Cell cell)
  {
    throw new UnsupportedOperationException();
  }
}