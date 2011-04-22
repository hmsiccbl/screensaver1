package edu.harvard.med.screensaver.io.workbook2;

import java.util.Iterator;

public class Row implements Iterable<Cell>
{
  private Worksheet _worksheet;
  private int _row;
  
  Row(Worksheet worksheet, int row)
  {
    _worksheet = worksheet;
    _row = row;
  }
  
  public Cell getCell(int column)
  {
    return getCell(column, false);
  }
  
  public Cell getCell(int column, boolean isRequired)
  {
    return _worksheet.getCell(column, _row, isRequired);
  }

  // TODO: check across full row
  public boolean isEmpty()
  {
    return getCell(0).isEmpty();
  }

  public int getColumns()
  {
    return _worksheet.getColumns();
  }
  
  public int getRow() { return _row; } 
  
  public String toString() { return "Row: " + _row; }

  public Iterator<Cell> iterator()
  {
    return new CellIterator(0,_worksheet.getColumns()-1);
  }
  
  public class CellIterator implements Iterator<Cell>
  {
    private int _current;
    private int _end;
      
    public CellIterator(int begin, int end)
    {
      _current = begin;
      _end = end;
    }
    
    public boolean hasNext()
    {
      return _end >= 0 && _current <= _end;
    }

    public Cell next()
    {
      return Row.this.getCell(_current++);
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
}
