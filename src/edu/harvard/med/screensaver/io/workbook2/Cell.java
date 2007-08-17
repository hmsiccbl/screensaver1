// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/Cell.java $
// $Id: Cell.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import jxl.BooleanCell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;

import org.apache.log4j.Logger;

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
 * returned by a previous {@link Factory#getCell} call, so if you need the
 * Cell to have a longer lifetime, clone it.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Cell
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(Cell.class);

  private static final String INVALID_CELL_TYPE_ERROR = "invalid cell type";
  private static final String CELL_VALUE_REQUIRED_ERROR = "value required";
  private static final Pattern DECIMAL_PRECISION_PATTERN = Pattern.compile(".*?(\\.([0#]+))?(%?)");
  private static final String GENERAL_FORMAT = "GENERAL";

  
  // static methods
  
  public static Date convertGmtDateToLocalTimeZone(Date date)
  {
    try {
      // all of the below nonsense is to convert the GMT-time zone date (as
      // returned from the workbook) to the local time zone; see
      // http://www.andykhan.com/jexcelapi/tutorial.html#dates
      DateFormat dateFormat = DateFormat.getDateInstance();
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      String formattedDate = dateFormat.format(date); // formatted output is w/o time zone
      dateFormat.setTimeZone(TimeZone.getDefault()); 
      Date convertedDate = dateFormat.parse(formattedDate); // convertedDate is now correct for current time zone
      return convertedDate;
    }
    catch (ParseException e) {
      // should never occur since we're using the DateFormat object to parse what it previously formatted 
      log.error(e);
      return date; 
    }
  }


  // instance data members
  
  protected ParseErrorManager _errors;
  protected Workbook _workbook;
  protected jxl.Sheet _sheet;
  protected jxl.Cell _cell;
  protected int _sheetIndex;
  private short _column;
  private int _row;
  private boolean _required;
  /**
   * Used to build formatted cell strings.
   */
  private StringBuilder _formattedRowAndColumnBuilder = new StringBuilder("(A,1)");
  
  // inner class definitions
  
  /**
   * Instantiates Cell objects with shared arguments. (For lisp-o-philes,
   * this is akin to "currying" a function.)
   * 
   * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
   * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
   */
  public static class Factory
  {
    private ParseErrorManager _errors;
    private Workbook _workbook;
    private int _sheetIndex;
    /**
     * @motivation we don't want to instantiate a new Cell object for every cell
     *             we must read, since we do this a whole lot
     */
    private Cell _recycledCell;
    private short _columnOffset;
    private int _rowOffset;
    

    /**
     * Constructs a Factory object that can be used instantiate Cell
     * objects with a shared set of arguments.
     */
    public Factory(Workbook workbook,
                   int sheetIndex,
                   ParseErrorManager errors)
    {
      _workbook = workbook;
      _sheetIndex = sheetIndex;
      _errors = errors;
    }
    
    /**
     * Constructs a Factory object that can be used instantiate Cell objects
     * with a shared set of arguments and a new origin, allowing requested Cell
     * coordinates to be made relative to this origin.
     * 
     * @param columnOffset the "x" component of the new origin, to which all requested Cell coordinates will be relative
     * @param rowOffset the "y" component of the new origin, to which all requested Cell coordinates will be relative
     */
    public Factory(Workbook workbook,
                   int sheetIndex,
                   short columnOffset,
                   int rowOffset,
                   ParseErrorManager errors)
    {
      _workbook = workbook;
      _sheetIndex = sheetIndex;
      _columnOffset = columnOffset;
      _rowOffset = rowOffset;
      _errors = errors;
    }
    
    /**
     * Get a (recycled) Cell object, initialized with this factory's shared
     * arguments. Subsequent calls return the same object with a modified
     * configuration!
     * 
     * @param column the physical zero-based column in the worksheet
     * @param row the physical zero-based row in the worksheet
     * @return a (recycled) Cell, ready to parse the value in the cell it's
     *         associated with
     */
    public Cell getCell(short column, int row, boolean required)
    {
      if (_recycledCell == null) {
        _recycledCell = new Cell(_workbook,
                                 _sheetIndex,
                                 _errors,
                                 column,
                                 row,
                                 required);
      }
      else {
        _recycledCell._column = (short) (column + _columnOffset);
        _recycledCell._row = row + _rowOffset;
        _recycledCell._required = required;
      }
      return _recycledCell;
    }
    
    /**
     * @see #getCell(short, int, boolean)
     */
    public Cell getCell(short column, int row)
    {
      return getCell(column, row, /* required= */false);
    }
    
    public Cell getNullCell()
    {
      return new NullCell(_workbook, _sheetIndex, _errors);
    }
  }
  

  // static methods

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
  public short getColumn() { return _column; }
  
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
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return 0.0;
        }
        return null;
      }
      Double value = new Double(((NumberCell) cell).getValue());
      return value;
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return 0.0;
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected a number)", this);
        return 0.0;
      }
      return null;
    }
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
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return 0;
        }
        return null;
      }
      return new Double(((NumberCell) cell).getValue()).intValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return 0;
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected a number)", this);
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
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return false;
        }
        return null;
      }
      if (!isBoolean()) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected boolean)", this);
        if (_required) {
          return false;
        }
        return null;
      }
      return ((BooleanCell) cell).getValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
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
  public Date getDate()
  {
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return new Date();
        }
        return null;
      }
      Date date = ((DateCell) cell).getDate();
      return convertGmtDateToLocalTimeZone(date);
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return new Date();
      }
      return null;
    }
    catch (ClassCastException e) {
      if (_required) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected a date)", this);
        return new Date();
      }
      return null;
    }
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
    try {
      jxl.Cell cell = getJxlCell();
      if (cell.getType() == CellType.EMPTY) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return "";
        }
        return null;
      }
      return cell.getContents();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
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

  protected Cell(
    Workbook workbook,
    int sheetIndex,
    ParseErrorManager errors,
    short column,
    int row,
    boolean required)
  {
    _workbook = workbook;
    _sheetIndex = sheetIndex;
    _sheet = workbook.getWorkbook().getSheet(sheetIndex);
    _errors = errors;
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
                    _errors,
                    _column,
                    _row,
                    _required);
  }
}
