// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

public class WellName
{
  // static members

  public static String toString(int rowIndex, int columnIndex)
  {
    return String.format("%c%02d", 
                         Well.MIN_WELL_ROW + rowIndex, 
                         columnIndex + Well.MIN_WELL_COLUMN);
  }

  
  // instance data members

  private int _rowIndex;
  private int _columnIndex;
  private String _wellName;

  
  // public constructors and methods
  
  public WellName(int rowIndex,
                  int columnIndex)
  {
    if (columnIndex >= Well.PLATE_COLUMNS || columnIndex < 0 ||
      rowIndex >= Well.PLATE_ROWS|| rowIndex < 0) {
      throw new IllegalArgumentException("row or column out of range");
    }
    _rowIndex = rowIndex;
    _columnIndex = columnIndex;
    _wellName = toString(_rowIndex, _columnIndex);
  }

  public WellName(char row, 
                  int column)
  {
    this(row - Well.MIN_WELL_ROW,
         column - Well.MIN_WELL_COLUMN);
  }
  
  public WellName(String wellName)
  {
    setName(wellName);
  }

  public int getColumnIndex()
  {
    return _columnIndex;
  }

  public void setColumnIndex(int columnIndex)
  {
    if (columnIndex >= Well.PLATE_COLUMNS) {
      throw new IllegalArgumentException("column index" + columnIndex + " is not < " + Well.PLATE_COLUMNS);
    }
    _columnIndex = columnIndex;
  }

  public int getRowIndex()
  {
    return _rowIndex;
  }

  public void setRowIndex(int rowIndex)
  {
    if (rowIndex >= Well.PLATE_ROWS) {
      throw new IllegalArgumentException("row index " + rowIndex + " is not < " + Well.PLATE_ROWS);
    }
    _rowIndex = rowIndex;
  }
  
  public void setName(String wellName)
  {
    setRowIndex(wellName.toUpperCase().charAt(0) - Well.MIN_WELL_ROW);
    setColumnIndex(Integer.parseInt(wellName.substring(1)) - 1);
    _wellName = toString(_rowIndex, _columnIndex);
  }
  
  public String getName()
  {
    return _wellName;
  }
  
  public String toString()
  {
    return _wellName;
  }

  @Override
  public int hashCode()
  {
    // TODO Auto-generated method stub
    return super.hashCode();
  }
  
  @Override
  public boolean equals(Object other)
  {
    if (other == this) {
      return true;
    }
    return _rowIndex == ((WellName) other)._rowIndex &&
    _columnIndex == ((WellName) other)._columnIndex;
  }

  
  // private methods

}

