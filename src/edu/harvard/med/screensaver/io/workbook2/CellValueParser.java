package edu.harvard.med.screensaver.io.workbook2;

import java.util.List;

/**
 * Interface for the various cell parsers used by the
 * {@link edu.harvard.med.screensaver.io.screenresults.ScreenResultParser}
 * class. Cell parsers read the value from a cell and convert to a more strictly
 * typed object, for use within our domain model.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
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