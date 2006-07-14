package edu.harvard.med.screensaver.io.workbook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;

/**
 * Parses the value of a cell, mapping a text value to an internal, system
 * object representation. Handles single-valued cells as well as cells that
 * contain lists of values. Note that this class is a non-static inner class
 * and references instance methods of {@link ScreenResultParser}.
 */
public class CellVocabularyParser<T> implements CellValueParser<T>
{
  // TODO: class methods needs javadocs
  
  // static data members
  
  private static final String DEFAULT_ERROR_MESSAGE = "unparseable value";
  private static final String DEFAULT_DELIMITER_REGEX = ",";

  
  // instance data members
  
  private SortedMap<String,T> _parsedValue2SystemValue;
  private T _valueToReturnIfUnparseable = null;
  private String _delimiterRegex = ",";
  private ParseErrorManager _errors;
  private String _errorMessage;
  
  
  // constructors

  public CellVocabularyParser(
    SortedMap<String,T> parsedValue2SystemValue,
    ParseErrorManager errors) 
  {
    this(parsedValue2SystemValue,
         null,
         errors,
         DEFAULT_ERROR_MESSAGE,
         DEFAULT_DELIMITER_REGEX);
  }
                         
  public CellVocabularyParser(SortedMap<String,T> parsedValue2SystemValue,
                              T valueToReturnIfUnparseable,
                              ParseErrorManager errors)
  {
    this(parsedValue2SystemValue,
         valueToReturnIfUnparseable,
         errors,
         DEFAULT_ERROR_MESSAGE,
         DEFAULT_DELIMITER_REGEX);
  }
  
  public CellVocabularyParser(SortedMap<String,T> parsedValue2SystemValue,
                              T valueToReturnIfUnparseable,
                              ParseErrorManager errors,
                              String errorMessage)
  {
    this(parsedValue2SystemValue,
         valueToReturnIfUnparseable,
         errors,
         errorMessage,
         DEFAULT_DELIMITER_REGEX);
  }
  
  public CellVocabularyParser(SortedMap<String,T> parsedValue2SystemValue,
                              T valueToReturnIfUnparseable,
                              ParseErrorManager errors,
                              String errorMessage,
                              String delimiterRegex)
  {
    _parsedValue2SystemValue = parsedValue2SystemValue;
    _valueToReturnIfUnparseable = valueToReturnIfUnparseable;
    _errors = errors;
    _errorMessage = errorMessage;
    _delimiterRegex = delimiterRegex;
  }
  
  
  // public methods

  public T parse(Cell cell) 
  {
    return doParse(cell.getString(), cell);
  }

  public List<T> parseList(Cell cell) {
    List<T> result = new ArrayList<T>();
    String textMultiValue = cell.getString();
    if (textMultiValue == null) {
      return result;
    }
    String[] textValues = textMultiValue.split(_delimiterRegex);
    for (int i = 0; i < textValues.length; i++) {
      String text = textValues[i];
      result.add(doParse(text, cell));
    }
    return result;
  }
  

  // private methods

  private T doParse(String text, Cell cell)
  {
    if (text == null) {
      return _valueToReturnIfUnparseable;
    }
    text = text.toLowerCase().trim();
    for (Iterator iter = _parsedValue2SystemValue.keySet()
                                                 .iterator(); iter.hasNext();) {
      String pattern = (String) iter.next();
      if (pattern.equalsIgnoreCase(text)) {
        return _parsedValue2SystemValue.get(pattern);
      }
    }
    _errors.addError(_errorMessage + " \"" + text + "\" (expected one of " + _parsedValue2SystemValue.keySet() + ")", cell);
    return _valueToReturnIfUnparseable;
  }

}