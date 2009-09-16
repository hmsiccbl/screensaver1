// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.util.AlphabeticCounter;

@Embeddable
public class WellName implements Comparable<WellName>
{
  // static members

  Pattern WELL_NAME_PATTERN = Pattern.compile("([A-Z]+)([0-9]+)");

  public static String toString(int rowIndex, int columnIndex)
  {
    return getRowLabel(rowIndex) + getColumnLabel(columnIndex);
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
    if (columnIndex < 0 || rowIndex < 0) {
      throw new IllegalArgumentException("row or column out of range");
    }
    _rowIndex = rowIndex;
    _columnIndex = columnIndex;
    _wellName = toString(_rowIndex, _columnIndex);
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

  private void setColumnIndex(int columnIndex)
  {
    if (columnIndex < 0) {
      throw new IllegalArgumentException("column index" + columnIndex + " is negative");
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

  private void setRowIndex(int rowIndex)
  {
    if (rowIndex < 0) {
      throw new IllegalArgumentException("row index" + rowIndex + " is negative");
    }
    _rowIndex = rowIndex;
  }

  @Transient
  public String getRowLabel()
  {
    return getRowLabel(_rowIndex);
  }

  @Transient
  public String getColumnLabel()
  {
    return getColumnLabel(_columnIndex);
  }

  public static String getRowLabel(int rowIndex)
  {
    return AlphabeticCounter.toLabel(rowIndex);
  }

  public static String getColumnLabel(int columnIndex)
  {
    return String.format("%02d", columnIndex + 1);
  }

  private void setName(String wellName)
  {
    Matcher matcher = WELL_NAME_PATTERN.matcher(wellName);
    if (matcher.matches()) {
      setRowIndex(parseRowLabel(matcher.group(1)));
      setColumnIndex(parseColumnLabel(matcher.group(2)));
    }
    else {
      throw new IllegalArgumentException("illegal well name '" + wellName + "'");
    }
    _wellName = toString(_rowIndex, _columnIndex);
  }

  public static int parseColumnLabel(String columnLabel)
  {
    return Integer.parseInt(columnLabel) - 1;
  }

  public static int parseRowLabel(String rowLabel)
  {
    return AlphabeticCounter.toIndex(rowLabel);
  }

  // TODO: length only works up to 384 well plates; increase length to accommodate labels for max plate size
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
    if (other == null) { 
      return false;
    }
    if (other == this) {
      return true;
    }
    return _rowIndex == ((WellName) other)._rowIndex &&
    _columnIndex == ((WellName) other)._columnIndex;
  }

  public int compareTo(WellName other)
  {
    return _rowIndex < other._rowIndex ? -1 : _rowIndex > other._rowIndex ? 1 : _columnIndex < other._columnIndex ? -1 : _columnIndex > other._columnIndex ? 1 : 0;
  }
}

