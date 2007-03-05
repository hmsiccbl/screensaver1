// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/Cell.java $
// $Id: Cell.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Encapsulates the parsing and error annotation operations for a cell in a
 * worksheet. Instantiate new Cells via a Cell.Factory, which you must first
 * instantiate with a set of arguments that will be common to a set of related
 * <code>Cell</code>s. The Cell returned by the factory may be the instance
 * returned by a previous {@link Factory#getHSSFCell()} call, so if you need the
 * Cell to have a longer lifetime, clone it.
 * 
 * @motivation explicitly associates sheet name with a cell, since you cannot
 *             get it from an HSSFSheet!
 * @motivation handle the fact that HSSFSheet cells can be undefined
 * @author ant
 */
public class Cell
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(Cell.class);

  private static final String INVALID_CELL_TYPE_ERROR = "invalid cell type";
  private static final String CELL_VALUE_REQUIRED_ERROR = "value required";
  private static final Pattern DECIMAL_PRECISION_PATTERN = Pattern.compile(".*?(\\.([0#]+))?(%?)");
  private static final String GENERAL_FORMAT = "GENERAL";


  // instance data members
  
  protected ParseErrorManager _errors;
  protected Workbook _workbook;
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
   * Instantiates Cell objects with shared arguments. For lisp-o-philes,
   * this is akin to "currying" a function.
   * 
   * @author ant
   */
  public static class Factory
  {
    private ParseErrorManager _errors;
    private Workbook _workbook;
    private int _sheetIndex;
    private String _sheetName;
    private HSSFSheet _sheet;
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
     * 
     * @param sheetName the name of the worksheet
     * @param sheet the worksheet itself
     * @param errors the error manager that will be notified of parse errors
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
     * @param sheetName the name of the worksheet
     * @param sheet the worksheet itself
     * @param columnOffset the "x" component of the new origin, to which all requested Cell coordinates will be relative
     * @param rowOffset the "y" component of the new origin, to which all requested Cell coordinates will be relative
     * @param errors the error manager that will be notified of parse errors
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

  /**
   * Utility method to set the value of an HSSFCell with a proper type (Numeric,
   * Date, Boolean, String), based upon the class of the <code>value</code>
   * argument. As a special case, if <code>value</code> happens to be a String
   * that can be interpreted as a Double, the cell type will set to double, not
   * string. If <code>value</code> is a Date or Calendar object, the cell's
   * style will also be set to a date format.
   */
  public static void setTypedCellValue(HSSFWorkbook workbook, HSSFCell cell, Object value)
  {
    if (value != null) {

      // allow us to handle Date and Calendar types in the same way
      if (value instanceof Calendar) {
        value = ((Calendar) value).getTime();
      }

      if (value instanceof Date) {
        cell.setCellValue((Date) value);
        HSSFCellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat((short) 0xe);
        cell.setCellStyle(dateStyle); // set date format to "m/d/yy"
      }
      else if (value instanceof Integer) {
        cell.setCellValue((Integer) value);
      }
      else if (value instanceof Number) {
        cell.setCellValue(((Number) value).doubleValue());
      }
      else if (value instanceof Boolean) {
        cell.setCellValue((Boolean) value);
      }
      else {
        try {
          double d = Double.parseDouble(value.toString());
          cell.setCellValue(d);
        }
        catch (NumberFormatException e) {
          cell.setCellValue(value.toString());
        }
      }
    }
  }

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
  public String getSheetName() { return _workbook.getWorkbook().getSheetName(_sheetIndex); }
  
  /**
   * Get the sheet containing the cell.
   * @return the sheet
   */
  public HSSFSheet getSheet() { return _workbook.getWorkbook().getSheetAt(_sheetIndex); }
  
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
      HSSFCell cell = getHSSFCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return 0.0;
        }
        return null;
      }
      Double value = new Double(cell.getNumericCellValue());
      // we need some special-case handling for formulas are expected to return a numeric value, but do not
      if (value.isNaN()) {
        throw new NumberFormatException("bad numeric value (maybe formula is not returning a numeric type?)");
      }
      return value;
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return 0.0;
      }
      return null;
    }
    catch (NumberFormatException e) {
      // note: we catch this exception, rather than checking, above, if cell
      // type is "Numeric", because cell type can also be "Formula", for which
      // getNumericCellValue() will still work
      _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected a double)", this);
      if (_required) {
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
      HSSFCell cell = getHSSFCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return 0;
        }
        return null;
      }
      return new Integer((int) cell.getNumericCellValue());
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return 0;
      }
      return null;
    }
    catch (NumberFormatException e) {
      // note: we catch this exception, rather than checking if cell type is
      // "Numeric", because it cell type can also be "Formula", and
      // getNumericCellValue() will still work
      _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected an integer)", this);
      if (_required) {
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
      HSSFCell cell = getHSSFCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return false;
        }
        return null;
      }
      if (cell.getCellType() != HSSFCell.CELL_TYPE_BOOLEAN) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected boolean)", this);
        if (_required) {
          return false;
        }
        return null;
      }
      return cell.getBooleanCellValue();
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
      HSSFCell cell = getHSSFCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return new Date();
        }
        return null;
      }
      return cell.getDateCellValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return new Date();
      }
      return null;
    }
    catch (NumberFormatException e) {
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
      HSSFCell cell = getHSSFCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        if (_required) {
          _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
          return "";
        }
        return null;
      }
      return cell.getStringCellValue();
    } 
    catch (CellOutOfRangeException e) {
      if (_required) {
        _errors.addError(CELL_VALUE_REQUIRED_ERROR, this);
        return "";
      }
      return null;
    }
    catch (NumberFormatException e) {
      if (_required) {
        _errors.addError(INVALID_CELL_TYPE_ERROR + " (expected a string)", this);
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
      HSSFCell cell = getHSSFCell();
      HSSFDataFormat dataFormat = _workbook.getWorkbook().createDataFormat();
      String formatStr = dataFormat.getFormat(cell.getCellStyle().getDataFormat());
      if (formatStr != null) {
        // note: I have witnessed both "GENERAL" and "General" from cell.getCellStyle().getDataFormat()
        if (formatStr.trim().equalsIgnoreCase(GENERAL_FORMAT)) {
          return -1;
        }
        Matcher matcher = DECIMAL_PRECISION_PATTERN.matcher(formatStr);
        if (matcher.matches()) {
          int decimalPrecision = 0;
          if (matcher.group(1) != null) {
            String decimalFormat = matcher.group(2);
            if (decimalFormat != null) {
              decimalPrecision = decimalFormat.length();
            }
          }
          if (matcher.group(3) != null && matcher.group(3).length() > 0) { // is a percentage
            decimalPrecision += 2;
          }
          return decimalPrecision;
        }
      }
      return -1;
    }
    catch (CellOutOfRangeException e) {
      return -1;
    }
  }
  
  public void annotateWithError(ParseError error)
  {
    HSSFCell cell = getOrCreateCell();
    
    HSSFWorkbook workbook = _workbook.getWorkbook();
    HSSFCellStyle errorStyle = workbook.createCellStyle();
    errorStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    errorStyle.setFillForegroundColor(HSSFColor.RED.index);
    cell.setCellStyle(errorStyle);
    String annotatedCellValue = getAsString(false);
    if (annotatedCellValue != null && annotatedCellValue.startsWith("ERROR: ")) {
      annotatedCellValue += "; ";
    }
    else {
      annotatedCellValue = "ERROR: ";
    }
    annotatedCellValue += error.getMessage();
    // HSSF requires us to null the cell before changing it's type! (or its throws an exception)
    cell.setCellValue((String) null);
    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
    cell.setCellValue(annotatedCellValue);
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
      int cellType = getHSSFCell().getCellType();
      switch (cellType) {
      case HSSFCell.CELL_TYPE_BLANK:
        if (withValidation) {
          return getString();
        }
        return "";
      case HSSFCell.CELL_TYPE_BOOLEAN:
        if (withValidation) {
          return getBoolean().toString();
        } 
        else {
          return Boolean.toString(getHSSFCell().getBooleanCellValue());
        }
      case HSSFCell.CELL_TYPE_NUMERIC: 
        if (withValidation) {
          return getDouble().toString();
        } 
        else {
          return Double.toString(getHSSFCell().getNumericCellValue());
        }
      case HSSFCell.CELL_TYPE_ERROR:
        return "<error cell>";
      case HSSFCell.CELL_TYPE_FORMULA: // will get the formula *result* as a string
      case HSSFCell.CELL_TYPE_STRING:
      default:
        if (withValidation) {
          return getString();
        } 
        else {
          return getHSSFCell().getStringCellValue();
        }
      }
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
  
  public int getType()
  {
    try {
      return getHSSFCell().getCellType();
    }
    catch (CellOutOfRangeException e) {
      return HSSFCell.CELL_TYPE_BLANK;
    }
  }


  /**
   * Determine if cell contains a numeric value, taking into account formula
   * type cells.
   * 
   * @param cell the cell to inspect
   * @return true iff cell is of type HSSFCell.CELL_TYPE_NUMERIC, or
   *         HSSFCell.CELL_TYPE_FORMULA and formula evaluates to a numeric type.
   */
  public boolean isNumeric()
  {
    if (getType() == HSSFCell.CELL_TYPE_NUMERIC) {
      return true;
    }
    // HSSF does not allow us to determine the cell type for the *value* (result) of a formula cell;
    // it could be numeric, but who can say? we perform trial and error (or more
    // accurately, "trial and exception") to figure it out
    if (getType() == HSSFCell.CELL_TYPE_FORMULA) {
      try {
        getHSSFCell().getNumericCellValue();
        // if no exception is thrown, then cell's formula evaluates to a numeric value
        return true;
      }
      catch (CellOutOfRangeException e) {
        // let caller handle this problem when it actually tries to read the cell
        return false; 
      }
      catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }
  
  /**
   * Determine if cell contains a boolean value, taking into account formula
   * type cells.
   * 
   * @param cell the cell to inspect
   * @return true iff cell is of type HSSFCell.CELL_TYPE_BOOLEAN, or
   *         HSSFCell.CELL_TYPE_FORMULA and formula evaluates to a boolean type.
   */
  public boolean isBoolean()
  {
    if (getType() == HSSFCell.CELL_TYPE_BOOLEAN) {
      return true;
    }
    // HSSF does not allow us to determine the cell type for the *value* (result) of a formula cell;
    // it could be numeric, but who can say? we perform trial and error (or more
    // accurately, "trial and exception") to figure it out
    if (getType() == HSSFCell.CELL_TYPE_FORMULA) {
      try {
        getHSSFCell().getBooleanCellValue();
        // if no exception is thrown, then cell's formula evaluates to a boolean value
        return true;
      }
      catch (CellOutOfRangeException e) {
        // let caller handle this problem when it actually tries to read the cell
        return false; 
      }
      catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }
  
  public boolean isEmpty()
  {
    try {
      return getHSSFCell().getCellType() == HSSFCell.CELL_TYPE_BLANK;
    }
    catch (CellOutOfRangeException e) {
      return true;
    }
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
    _errors = errors;
    _column = column;
    _row = row;
    _required = required;
  }
  


  /**
   * Returns the HSSFCell on the worksheet at the specified location.
   * 
   * @return an <code>HSSFCell</code>
   * @throws CellOutOfRangeException if the specified cell has not been
   *           initialized with a value in the worksheet
   */
  protected HSSFCell getHSSFCell()
    throws CellOutOfRangeException
  {
    HSSFRow row = getSheet().getRow(getRow());
    if (row == null) {
      throw new CellOutOfRangeException(CellOutOfRangeException.UndefinedInAxis.ROW,
                                        this);
    }
    HSSFCell cell = row.getCell(getColumn());
    if (cell == null) {
      throw new CellOutOfRangeException(CellOutOfRangeException.UndefinedInAxis.COLUMN,
                                        this);
    }
    return cell;
  }
  
  private HSSFCell getOrCreateCell()
  {
    return HSSFCellUtil.getCell(HSSFCellUtil.getRow(getRow(), getSheet()), getColumn());
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
