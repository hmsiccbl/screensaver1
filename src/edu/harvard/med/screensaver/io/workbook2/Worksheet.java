package edu.harvard.med.screensaver.io.workbook2;

import java.util.Iterator;

import jxl.Sheet;

/**
 * Provides access to {@link Cell cells} on this Worksheet. 
 */
public class Worksheet implements Iterable<Row>
{
  private Workbook _workbook;
  private int _sheetIndex;
  /**
   * @motivation we don't want to instantiate a new Cell object for every cell
   *             we must read, since we expect to do this a whole lot
   */
  private Cell _recycledCell;
  private int _columnOffset;
  private int _rowOffset;
  private Sheet _sheet;
  

  /**
   * Constructs a Worksheet object that can be used instantiate Cell
   * objects with a shared set of arguments.
   * @see Workbook#getWorksheet(int)
   */
  Worksheet(Workbook workbook, int sheetIndex)
  {
    _workbook = workbook;
    _sheetIndex = sheetIndex;
  }
  
  /**
   * Clone this worksheet, and set a new cell origin for convenience when parsing
   * @param columnOffset
   * @param rowOffset
   * @return new Worksheet with specified origin
   */
  public Worksheet forOrigin(int columnOffset, int rowOffset)
  {
    Worksheet worksheet = new Worksheet(_workbook, _sheetIndex);
    worksheet._columnOffset = columnOffset;
    worksheet._rowOffset = rowOffset;
    return worksheet;
  }
  
  /**
   * Returns an iterator that iterates from the given row index until the last defined row in the worksheet.
   * @param fromRow
   * @return
   */
  public Iterator<Row> iterator()
  {
    return new RowIterator(0, getWorksheet().getRows()-1);
  }
  
  Sheet getWorksheet() 
  {
    if (_sheet == null) {
      _sheet = _workbook.getWorkbook().getSheet(_sheetIndex);
    }
    return _sheet;
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
  public Cell getCell(int column, int row, boolean required)
  {
    if (_recycledCell == null) {
      _recycledCell = new Cell(_workbook,
                               _sheetIndex,
                               column + _columnOffset,
                               row + _rowOffset,
                               required);
    }
    else {
      _recycledCell._column = column + _columnOffset;
      _recycledCell._row = row + _rowOffset;
      _recycledCell._required = required;
    }
    return _recycledCell;
  }
  
  /**
   * @see #getCell(short, int, boolean)
   */
  public Cell getCell(int column, int row)
  {
    return getCell(column, row, /* required= */false);
  }

  protected Workbook getWorkbook()
  {
    return _workbook;
  }

  int getColumnOffset()
  {
    return _columnOffset;
  }
  
  public class RowIterator implements Iterator<Row>
  {
    private int _row;
    private int _toRow;
      
    /**
     * Create RowIterator that iterates between the specified range of rows.
     * @param fromRow the first row index to be iterated, zero-based, inclusive
     * @param toRow the last row index to be iterated, zero-based, inclusive; must be <= max supported row index by worksheet
     */
    public RowIterator(int fromRow, int toRow)
    {
      _row = fromRow;
      _toRow = toRow;
    }
    
    public boolean hasNext()
    {
      return _toRow >= 0 && _row <= _toRow;
    }

    public Row next()
    {
      return new Row(Worksheet.this,  _row++);
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  public String getName()
  {
    return getWorksheet().getName();
  }

  public int getSheetIndex()
  {
    return _sheetIndex;
  }

  public int getRows()
  {
    return getWorksheet().getRows();
  }

  public void addWorkbookError(String string)
  {
    _workbook.addError(string + ", sheet: " + getName());    
  }

  public Row getRow(int i)
  {
    return new Row(this,i);
  }  
  
  public int getColumns() { return this.getWorksheet().getColumns(); }

}