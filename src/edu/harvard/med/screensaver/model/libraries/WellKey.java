// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @motivation Allows ResultValueType.resultValues map to have keys that is not
 *             the full-blown Well entity. Allows the map values to be accessed
 *             without always having a Well object handy, and which can be
 *             expensive to obtain. It is reasonable that a portion of our
 *             application would know what plate number/row/column it wants a
 *             result value for, without already having an actual Well object.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WellKey implements Comparable
{
  // static members

  private static Logger log = Logger.getLogger(WellKey.class);

  // TODO: merge with LibrariesController._wellNamePattern
  private static Pattern keyPattern = Pattern.compile("(\\d+):([A-P])(\\d+)");
  
  // instance data members
  private int _plateNumber;
  private int _row;
  private int _column;

  private transient String _wellName;
  private transient int _hashCode;


  // public constructors and methods
  
  private WellKey()
  {
  }

  public WellKey(int plateNumber, int row, int column)
  {
    _plateNumber = plateNumber;
    setRow(row);
    setColumn(column);
  }
  
  public WellKey(int plateNumber, String wellName)
  {
    _plateNumber = plateNumber;
    setWellName(wellName);
  }
  
  public WellKey(String key)
  {
    setKey(key);
  }
  
  public String getKey()
  {
    return toString();
  }
  
  public void setKey(String key)
  {
    resetDerivedValues();
    Matcher matcher = keyPattern.matcher(key);
    if (matcher.matches()) {
      _plateNumber = Integer.parseInt(matcher.group(1));
      setRow(matcher.group(2).toUpperCase().charAt(0) - Well.MIN_WELL_ROW);
      setColumn(Integer.parseInt(matcher.group(3)) - 1);
//      if (log.isDebugEnabled()) {
//        log.debug("decomposed key " + key + " to " + this);
//      }
    } 
    else {
      throw new IllegalArgumentException("invalid composite well key string '" + key + "'");
    }
  }

  public int getColumn()
  {
    return _column;
  }

  public void setColumn(int column)
  {
    if (column >= Well.PLATE_COLUMNS) {
      throw new IllegalArgumentException("column " + column + " is not < " + Well.PLATE_COLUMNS);
    }
    resetDerivedValues();
    _column = column;
  }

  public int getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(int plateNumber)
  {
    _plateNumber = plateNumber;
  }

  public int getRow()
  {
    return _row;
  }

  public void setRow(int row)
  {
    if (row >= Well.PLATE_ROWS) {
      throw new IllegalArgumentException("row " + row + " is not < " + Well.PLATE_ROWS);
    }
    resetDerivedValues();
    _row = row;
  }
  
  private void resetDerivedValues()
  {
    _wellName = null;
    _hashCode = -1;
  }

  public boolean equals(Object o)
  {
    if (o == this) { 
      return true;
    }
    WellKey other = (WellKey) o;
    return _plateNumber == other._plateNumber && _row == other._row && _column == other._column;
  }
  
  public int hashCode()
  {
    if (_hashCode == -1) {
      _hashCode = _plateNumber * (Well.PLATE_ROWS * Well.PLATE_COLUMNS) + _row * Well.PLATE_COLUMNS + _column;
    }
    return _hashCode;
  }
  
  public String toString()
  {
    return String.format("%0" + Well.PLATE_NUMBER_LEN + "d:%s", _plateNumber, getWellName());
  }
  
  public int compareTo(Object o)
  {
    WellKey other = (WellKey) o;
    int hashCode1 = hashCode();
    int hashCode2 = other.hashCode();
    return hashCode1 < hashCode2 ? -1 : hashCode1 > hashCode2 ? 1 : 0;
  }

  public void setWellName(String wellName)
  {
    setRow(wellName.toUpperCase().charAt(0) - Well.MIN_WELL_ROW);
    setColumn(Integer.parseInt(wellName.substring(1)) - 1);
  }

  public String getWellName()
  {
    if (_wellName == null) {
      _wellName = String.format("%c%02d", 
                                Well.MIN_WELL_ROW + _row, 
                                _column + Well.MIN_WELL_COLUMN);
    }
    return _wellName;
  }

  // private methods

}

