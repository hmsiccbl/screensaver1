// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * Encapsulates the parsing operations for a cell in a worksheet. Instantiate
 * new CellParsers via a CellReader.Factory, which you must first instantiate
 * with a set of arguments that will be common to a "class" of CellParsers.
 * 
 * @motivation convenience class for reading HSSFSheet cells
 * @motivation explicitly associates sheet name with a cell, since you cannot
 *             get it from an HSSFSheet!
 * @author ant
 */
public class CellReader
{
  
  // static data members
  
  public static final String INVALID_CELL_TYPE_ERROR = "invalid cell type";
  
  
  // instance data members
  
  private ParseErrorManager _errors;
  private String _sheetName;
  private HSSFSheet _sheet;
  private short _column;
  private short _row;
  
  // inner class definitions
  
  /**
   * Instantiates CellReader objects with shared arguments. For lisp-o-philes,
   * this is akin to "currying" a function.
   * 
   * @author ant
   */
  public static class Factory
  {
    private ParseErrorManager _errors;
    private String _sheetName;
    private HSSFSheet _sheet;
    private CellReader _recycledCellReader;
    

    /**
     * Constructs a Factory object that can be used instantiate CellReader
     * objects with a shared set of arguments
     * 
     * @param sheetName the name of the worksheet
     * @param sheet the worksheet itself
     * @param errors the error manager that will be notified of parse errors
     */
    public Factory(String sheetName,
                   HSSFSheet sheet,
                   ParseErrorManager errors)
    {
      _sheet = sheet;
      _sheetName = sheetName;
      _errors = errors;
    }
    
    /**
     * Instantiate a new CellReader, using this factory's shared arguments.
     * 
     * @param column the physical zero-based column in the worksheet
     * @param row the physical zero-based row in the worksheet
     * @return a brand new CellReader, ready to parse the value in the cell it's
     *         associated with
     */
    public CellReader newCellReader(short column,
                                    short row)
    {
      if (_recycledCellReader == null ) {
        _recycledCellReader = new CellReader(_sheetName,
                                             _sheet,
                                             _errors,
                                             column,
                                             row);
      } else {
        _recycledCellReader._column = column;
        _recycledCellReader._row = row;
      }
      return _recycledCellReader;
    }
  }
  

  // public methods
  
  /**
   * Get the sheet name.
   * @return the sheet name
   */
  public String getSheetName() { return _sheetName; }
  
  /**
   * Get the sheet containing the cell to be parsed.
   * @return the sheet
   */
  public HSSFSheet getSheet() { return _sheet; }
  
  /**
   * Get the column containing the cell to be parsed.
   * @return the column
   */
  public short getColumn() { return _column; }
  
  /**
   * Get the row containing the cell to be parsed.
   * @return the row
   */
  public short getRow() { return _row; }
  
  /**
   * A human-readable representation of the cell's location in the workbook.
   * @return a <code>String</code>
   */
  public String toString() {
    // TODO: only handles A-Z, not AA...
    return _sheetName + ":(" + Character.toString((char) (_column + 'A')) + ", " + (_row + 1) + ")";
  }
  
  /**
   * Get a <code>Double</code> value from the cell.
   * 
   * @return a <code>Double</code> value or
   *         <code>0.0</false> if cell does not contain a valid (or any) double value
   */
  public Double getDouble() 
  {
    try {
      HSSFCell cell = getCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        return 0.0;
      }
      if (cell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC) {
        _errors.addError(INVALID_CELL_TYPE_ERROR, this);
        return 0.0;
      }
      return new Double(cell.getNumericCellValue());
    } 
    catch (CellUndefinedException e) {
      return new Double(0.0);
    }
  }
  
  /**
   * Get an <code>Integer</code> value for the cell.
   * 
   * @return an <code>Integer</code> value or
   *         <code>0</false> if cell does not contain a valid (or any) boolean value
   */
  public Integer getInteger()
  {
    try {
      HSSFCell cell = getCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        return 0;
      }
      if (cell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC) {
        _errors.addError(INVALID_CELL_TYPE_ERROR, this);
        return 0;
      }
      return new Integer((int) cell.getNumericCellValue());
    } 
    catch (CellUndefinedException e) {
      return 0;
    }
  }
  
  /**
   * Get an <code>Boolean</code> value for the cell.
   *
   * @return a <code>Boolean</code> value, or
   *         <code>false</false> if cell does not contain a valid (or any) boolean value
   */
  public Boolean getBoolean()
  {
    try {
      HSSFCell cell = getCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        return false;
      }
      if (cell.getCellType() != HSSFCell.CELL_TYPE_BOOLEAN) {
        _errors.addError(INVALID_CELL_TYPE_ERROR, this);
        return false;
      }
      return cell.getBooleanCellValue();
    } 
    catch (CellUndefinedException e) {
      return false;
    }
  }
  
  /**
   * Get an <code>Date</code> value for the cell.
   * 
   * @return a <code>Date</code> value, or null if cell does not contain a
   *         valid (or any) date
   */
  public Date getDate()
  {
    try {
      HSSFCell cell = getCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        return null;
      }
      return cell.getDateCellValue();
    } 
    catch (CellUndefinedException e) {
      return null;
    }
    catch (NumberFormatException e) {
      _errors.addError(INVALID_CELL_TYPE_ERROR, this);
      return null;
    }
  }
  
  /**
   * Get an <code>String</code> value for the cell.
   * 
   * @return a <code>String</code> value, or the empty string if cell does not
   *         contain a value
   */
  public String getString()
  {
    try {
      HSSFCell cell = getCell();
      if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
        return "";
      }
      return cell.getStringCellValue();
    } 
    catch (CellUndefinedException e) {
      return "";
    }
    catch (NumberFormatException e) {
      _errors.addError(INVALID_CELL_TYPE_ERROR, this);
      return null;
    }
  }  
  
  // protected and private methods and constructors
  
  protected CellReader(String sheetName,
                       HSSFSheet sheet,
                       ParseErrorManager errors,
                       short column,
                       short row)
  {
    _sheet = sheet;
    _sheetName = sheetName;
    _errors = errors;
    _column = column;
    _row = row;
  }
  
  /**
   * Returns the HSSFCell on the worksheet at the specified location.
   * 
   * @return an <code>HSSFCell</code>
   * @throws CellUndefinedException if the specified cell has not been
   *           initialized with a value in the worksheet
   */
  private HSSFCell getCell()
    throws CellUndefinedException
  {
    if (_sheet.getLastRowNum() < getRow()) {
      throw new CellUndefinedException(CellUndefinedException.UndefinedInAxis.ROW,
                                       this);
    }
    HSSFRow row = _sheet.getRow(getRow());
    if (row.getLastCellNum() < getColumn()) {
      throw new CellUndefinedException(CellUndefinedException.UndefinedInAxis.COLUMN,
                                       this);
    }
    HSSFCell cell = row.getCell(getColumn());
    if (cell == null) {
      throw new CellUndefinedException(CellUndefinedException.UndefinedInAxis.ROW_AND_COLUMN,
                                       this);
    }
    return cell;
  }
  
}
