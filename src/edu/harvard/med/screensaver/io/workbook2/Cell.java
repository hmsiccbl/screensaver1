// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.regex.Pattern;

import jxl.BooleanCell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Encapsulates the parsing and error annotation operations for a cell in a
 * worksheet. Also, allows the client code to reference a cell by its position,
 * even if that cell is undefined in the workbook; undefined workbook cells are
 * treated as empty cells, allowing client code to ignore how underlying Excel
 * API handles these undefined cells (e.g., JExcelAPI throws
 * ArrayOutofBoundsExceptions for any cells below or to the right of the
 * lower-right-most defined cell, and treats undefined cells within that
 * rectangle as empty).
 * <p>
 * Instantiate new Cells via a Cell.Factory, which must first be instantiated
 * with a set of arguments that will be common to a set of related
 * <code>Cell</code>s. The Cell returned by the factory may be the instance
 * returned by a previous {@link Worksheet#getCell} call, so if you need the
 * Cell to have a longer lifetime, clone it.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Cell
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(Cell.class);

  public static final String INVALID_CELL_TYPE_ERROR = "invalid cell type";
  public static final String CELL_VALUE_REQUIRED_ERROR = "value required";

  
  // static methods
  
  public static LocalDateTime convertGmtDateToLocalTimeZone(Date date)
  {
    return new LocalDateTime(date.getTime(), DateTimeZone.UTC);
  }


  // instance data members
  
  protected Workbook _workbook;
  protected jxl.Sheet _sheet;
  protected jxl.Cell _cell;
  protected int _sheetIndex;
  int _column;
  int _row;
  boolean _required;
  /**
   * Used to build formatted cell strings.
   */
  private StringBuilder _formattedRowAndColumnBuilder = new StringBuilder("(A,1)");
  
  // inner class definitions
  
  public static String columnIndexToLabel(int columnIndex)
  {
    String columnLabel = "";
    if (columnIndex >= 26) {
      columnLabel += (char) ('A' + (columnIndex / 26) - 1);
    }
    columnLabel += (char) ('A' + (columnIndex % 26));
    return columnLabel;
  }

  public static int columnLabelToIndex(String columnLabel)
    throws IllegalArgumentException
  {
    if (!columnLabel.matches("^[A-Za-z][A-Za-z]?$")) {
      throw new IllegalArgumentException("malformed column label string:" + columnLabel);
    }
    
    columnLabel = columnLabel.toUpperCase();
    int columnIndex= 1;
    if (columnLabel.length() == 2) {
      columnIndex = 26 * (columnLabel.charAt(0) - 'A' + 1) + (columnLabel.charAt(1) - 'A');
    }
    else if (columnLabel.length() == 1) {
      columnIndex = columnLabel.charAt(0) - 'A';
    }
    return columnIndex;
  }

  
  // public methods
  
  /**
   * Get the workbook containing the cell.
   * @return the sheet name
   */
  public Workbook getWorkbook()
  {
    return _workbook; 
  }

  /**
   * Get the sheet name.
   * @return the sheet name
   */
  public String getSheetName() { return _sheet.getName(); }
  
  /**
   * Get the sheet containing the cell.
   * @return the sheet
   */
  public Sheet getSheet() { return _sheet; }
  
  /**
   * Get the column containing the cell.
   * @return the column
   */
  public int getColumn() { return _column; }
  
  /**
   * Get the row containing the cell.
   * @return the row
   */
  public int getRow() { return _row; }
  
  public String getFormattedRowAndColumn()
  {

    _formattedRowAndColumnBuilder.setLength(0);
    _formattedRowAndColumnBuilder.append('(');
    _formattedRowAndColumnBuilder.append(columnIndexToLabel(_column));
    _formattedRowAndColumnBuilder.append(',');
    _formattedRowAndColumnBuilder.append(Integer.toString(_row + 1));
    _formattedRowAndColumnBuilder.append(')');
    return _formattedRowAndColumnBuilder.toString();
  }
  
  public boolean isRequired() { return _required; }
  
  /**
   * A human-readable representation of the cell's location in the workbook.
   * @return a <code>String</code>
   */
  public String toString() {
    return /*_workbook.getWorkbookFile().getName() + ":" +*/ 
    getSheetName() + ":" + getFormattedRowAndColumn();
  }
  
  /**
   * Get a <code>Double</code> value from the cell.
   * 
   * @return a <code>Double</code> value if cell contains a valid double 
   *         value; if cell does not contain a double or an error occurs
   *         <code>null</code> is returned, unless cell is required, in which
   *         case <code>0.0</code> is returned (to allow parsing
   *         code to proceed w/a default value) contain a value
   */
  public Double getDouble() 
  {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          addError(CELL_VALUE_REQUIRED_ERROR);
          return 0.0;
        }
        return null;
      }
      Double value = new Double(((NumberCell) cell).getValue());
      return value;
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        addError(CELL_VALUE_REQUIRED_ERROR);
        return 0.0;
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        addError(INVALID_CELL_TYPE_ERROR + " (expected a number)");
        return 0.0;
      }
      return null;
    }
  }
  
  /**
   * Get a <code>BigDecimal</code> value from the cell.
   * 
   * @return a <code>BigDecimal</code> value if cell contains a valid numeric 
   *         value; if cell does not contain a number or an error occurs
   *         <code>null</code> is returned, unless cell is required, in which
   *         case <code>0.0</code> is returned (to allow parsing
   *         code to proceed w/a default value) contain a value
   */
  public BigDecimal getBigDecimal()
  {
    Double value = getDouble();
    if (value != null) {
      return new BigDecimal(value.toString());
    }
    return null;
  }

  /**
   * Get an <code>Integer</code> value for the cell.
   * 
   * @return an <code>Integer</code> value if cell contains a valid integer
   *         value; if cell does not contain an integer or an error occurs
   *         <code>null</code> is returned, unless cell is required, in which
   *         case <code>0</code> is returned (to allow parsing
   *         code to proceed w/a default value) contain a value
   */
  public Integer getInteger()
  {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          addError(CELL_VALUE_REQUIRED_ERROR);
          return 0;
        }
        return null;
      }
      return new Double(((NumberCell) cell).getValue()).intValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        addError(CELL_VALUE_REQUIRED_ERROR);
        return 0;
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        addError(INVALID_CELL_TYPE_ERROR + " (expected a number)");
        return 0;
      }
      return null;
    }
  }
  
  /**
   * Get an <code>Boolean</code> value for the cell.
   * 
   * @return a <code>Boolean</code> value if cell contains a valid boolean
   *         value; if cell does not contain a boolean or an error occurs
   *         <code>null</code> is returned, unless cell is required, in which
   *         case <code>Boolean.FALSE</code> is returned (to allow parsing
   *         code to proceed w/a default value) contain a value
   */
  public Boolean getBoolean()
  {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          addError(CELL_VALUE_REQUIRED_ERROR);
          return false;
        }
        return null;
      }
      if (!isBoolean()) {
        addError(INVALID_CELL_TYPE_ERROR + " (expected boolean)");
        if (_required) {
          return false;
        }
        return null;
      }
      return ((BooleanCell) cell).getValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        addError(CELL_VALUE_REQUIRED_ERROR);
        return false;
      }
      return null;
    }
  }
  
  /**
   * Get an <code>Date</code> value for the cell.
   * 
   * @return a <code>Date</code> value if cell contains a valid date; if cell
   *         does not contain a date or an error occurs <code>null</code> is
   *         returned, unless cell is required, in which case the current date
   *         is returned (to allow parsing code to proceed w/a default value)
   *         contain a value
   */
  public LocalDateTime getDate()
  {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          addError(CELL_VALUE_REQUIRED_ERROR);
          return new LocalDateTime();
        }
        return null;
      }
      Date date = ((DateCell) cell).getDate();
      return convertGmtDateToLocalTimeZone(date);
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        addError(CELL_VALUE_REQUIRED_ERROR);
        return new LocalDateTime();
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        addError(INVALID_CELL_TYPE_ERROR + " (expected a date)");
        return new LocalDateTime();
      }
      return null;
    }
  }

  /**
   * Convenience method to get a LocaleDate from a date cell, when cell might be
   * empty. Behaves same as {@link #getDate()}.
   */
  public LocalDate getLocalDate()
  {
    LocalDateTime date = getDate();
    if (date == null) {
      return null;
    }
    return date.toLocalDate();
  }
  
  /**
   * Get an <code>String</code> value for the cell.
   * 
   * @return a <code>String</code> value if cell contains a string; if cell
   *         does not contain a string or an error occurs <code>null</code> is
   *         returned, unless cell is required, in which case the empty string
   *         is returned (to allow parsing code to proceed w/a default value)
   *         contain a value
   */
  public String getString()
  {
    return getString(_required);
  }
  
  /**
   * @param required if <code>true</code>, performs "is required"
   *          validation and annotates cell with error as necessary; set to
   *          false, if you need to query the value of the cell, without
   *          generating a validation error.
   */
  public String getString(boolean required)
    {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (required) {
          addError(CELL_VALUE_REQUIRED_ERROR);
          return "";
        }
        return null;
      }
      return cell.getContents();
    } 
    catch (CellOutOfRangeException e) {
      if (required) {
        addError(CELL_VALUE_REQUIRED_ERROR);
        return "";
      }
      return null;
    }
  }

  /**
   * Get the number of digits requested to appear after the decimal point, based
   * upon the cell's style.
   * 
   * @return the number of digits requested to appear after the decimal point;
   *         -1 no precision was defined by the cell's style, or if the cell
   *         style cannot be determined for any reason
   */
  public int getDoublePrecision()
  {
    try {
      if (!isNumeric()) {
        return -1;
      }
      jxl.NumberCell cell = (NumberCell) getJxlCell();
      NumberFormat numberFormat = cell.getNumberFormat();
      int precision = numberFormat.getMaximumFractionDigits();
      // HACK: percentages have precision + 2
      if (numberFormat.format(1.0).endsWith("%")) {
        precision += 2;
      }
      return precision; 
    }
    catch (CellOutOfRangeException e) {
      return -1;
    }
  }
  
  /**
   * Returns the value of the cell as a String, regardless of the cell's type.
   * If withValidation is true, parse errors will be added as necessary.
   * 
   * @param withValidation if <code>true</code>, performs "is required"
   *          validation and annotates cell with error as necessary; set to
   *          false, if you need to query the value of the cell, without
   *          generating a validation error.
   * @return a String representation of the cell's contents, regardless of the
   *         cell's actual type; if cell type is "error" then the String "<error
   *         cell>" is returned; if cell is undefined, empty string is returned
   */
  public String getAsString(boolean withValidation)
  {
    try {
      jxl.Cell cell = getJxlCell();
      CellType cellType = cell.getType();
      if (cellType.equals(CellType.EMPTY)) {
        if (withValidation) {
          return getString();
        }
        return "";
      }
      if (cellType.equals(CellType.BOOLEAN) ||
        cellType.equals(CellType.BOOLEAN_FORMULA)) {
        if (withValidation) {
          return getBoolean().toString();
        } 
        else {
          return Boolean.toString(((BooleanCell) cell).getValue());
        }
      }
      if (cellType.equals(CellType.NUMBER) ||
        cellType.equals(CellType.NUMBER_FORMULA)) {
        if (withValidation) {
          return getDouble().toString();
        } 
        else {
          return Double.toString(((NumberCell) cell).getValue());
        }
      }
      if (cellType.equals(CellType.DATE) ||
        cellType.equals(CellType.DATE_FORMULA)) {
        if (withValidation) {
          return getDouble().toString();
        } 
        else {
          return ((DateCell) cell).getDate().toString();
        }
      }
      if (cellType.equals(CellType.ERROR) ||
        cellType.equals(CellType.FORMULA_ERROR)) {
        return "<error cell>";
      }
      if (cellType.equals(CellType.LABEL) ||
        cellType.equals(CellType.STRING_FORMULA)) {
        if (withValidation) {
          return getString();
        } 
        else {
          return cell.getContents();
        }
      }
      return "";
    } 
    catch (CellOutOfRangeException e) {
      return "";
    }
  }
  
  /**
   * Return the value of the cell as a String. If cell value is required,
   * validation will be performed.
   * 
   * @return the value of the cell as a String, regardless of the cell type in
   *         the worksheet.
   * @see #getAsString(boolean)
   */
  public String getAsString()
  {
    return getAsString(_required);
  }
  
  public CellType getType()
  {
    try {
      return getJxlCell().getType();
    }
    catch (CellOutOfRangeException e) {
      return CellType.EMPTY;
    }
  }


  /**
   * Determine if cell contains a numeric value, taking into account formula
   * type cells.
   * 
   * @return true iff cell is of type HSSFCell.CELL_TYPE_NUMERIC, or
   *         HSSFCell.CELL_TYPE_FORMULA and formula evaluates to a numeric type.
   */
  public boolean isNumeric()
  {
    if (getType().equals(CellType.NUMBER) ||
      getType().equals(CellType.NUMBER_FORMULA)) {
      return true;
    }
    return false;
  }
  
  /**
   * Determine if cell contains a boolean value, taking into account formula
   * type cells.
   * 
   * @return true iff cell is of type HSSFCell.CELL_TYPE_BOOLEAN, or
   *         HSSFCell.CELL_TYPE_FORMULA and formula evaluates to a boolean type.
   */
  public boolean isBoolean()
  {
    if (getType().equals(CellType.BOOLEAN) ||
      getType().equals(CellType.BOOLEAN_FORMULA)) {
      return true;
    }
    return false;
  }
  
  public boolean isEmpty()
  {
    return getType().equals(CellType.EMPTY);
  }


  // protected and private methods and constructors

  Cell(Workbook workbook,
       int sheetIndex,
       int column,
       int row,
       boolean required)
  {
    _workbook = workbook;
    _sheetIndex = sheetIndex;
    _sheet = workbook.getWorkbook().getSheet(sheetIndex);
    _column = column;
    _row = row;
    _required = required;
  }
  


  /**
   * Returns the jxl.Cell on the worksheet at the specified location.
   * 
   * @return a <code>jxl.Cell</code>
   * @throws CellOutOfRangeException if the specified cell has not been
   *           initialized with a value in the worksheet
   */
  protected jxl.Cell getJxlCell()
    throws CellOutOfRangeException
  {
    try {
      jxl.Cell cell = getSheet().getCell(getColumn(), getRow());
      if (cell == null) {
        throw new CellOutOfRangeException(this);
      }
      return cell;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new CellOutOfRangeException(this);
    }
  }
  
  @Override
  /**
   * @motivation A single Cell object is recycled, to cut down on memory usage;
   *             if you need to keep a Cell around for longer than just parsing
   *             it (e.g. to remember the location of a parse error), you must
   *             call this method to create a clone of the Cell.
   */
  public Object clone()
  {
    return new Cell(_workbook,
                    _sheetIndex,
                    _column,
                    _row,
                    _required);
  }

  public void addError(String msg)
  {
    _workbook.getParseErrorManager().addError(msg, this);
  }
}
