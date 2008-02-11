// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class WellName implements Comparable<WellName>
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

  /**
   * @motivation for Hibernate
   */
  private WellName()
  {
  }

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

  /**
   * Return the 0-based index of the column.
   */
  @Transient
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

  /**
   * Return the 0-based index of the row.
   */
  @Transient
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

  /**
   * Return the letter "name" of the row ('A' through 'P')
   */
  @Transient
  public Character getRowName()
  {
    return Character.valueOf((char) (Well.MIN_WELL_ROW + _rowIndex));
  }

  /**
   * Return the numeric "name" of the column ('1' through '24').
   */
  @Transient
  public Integer getColumnName()
  {
    return _columnIndex + Well.MIN_WELL_COLUMN;
  }

  public void setName(String wellName)
  {
    setRowIndex(wellName.toUpperCase().charAt(0) - Well.MIN_WELL_ROW);
    setColumnIndex(Integer.parseInt(wellName.substring(1)) - 1);
    _wellName = toString(_rowIndex, _columnIndex);
  }

  @Column(name="wellName", length=3)
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
    return _wellName.hashCode();
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

  public int compareTo(WellName other)
  {
    return _wellName.compareTo(other._wellName);
  }

  // private methods

}

