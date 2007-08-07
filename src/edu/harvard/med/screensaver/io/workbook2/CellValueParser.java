package edu.harvard.med.screensaver.io.workbook2;

import java.util.List;

/**
 * Interface for the various cell parsers used by the
 * {@link ScreenResultParser} class. Cell parsers read the value from a cell
 * and convert to a more strictly typed object, for use within our entity
 * model. (This interface is probably a case of over-engineering.)
 * 
 * @author ant
 */
public interface CellValueParser<T>
{
  /**
   * Parse the value in a cell, returning <T>
   * @param cell the cell to be parsed
   * @return a <T>, representing the value of the cell
   */
  T parse(Cell cell);
  
  List<T> parseList(Cell cell);
}