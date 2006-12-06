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

  private static Pattern keyPattern = Pattern.compile("(\\d+):([A-P])(\\d+)");
  
  // instance data members
  private int _plateNumber;
  private int _row;
  private int _column;


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
    this(plateNumber + ":" + wellName);
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
    Matcher matcher = keyPattern.matcher(key);
    if (matcher.matches()) {
      _plateNumber = Integer.parseInt(matcher.group(1));
      setRow(matcher.group(2).charAt(0) - 'A');
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
    _row = row;
  }
  
  public boolean equals(Object o)
  {
    WellKey other = (WellKey) o;
    return _plateNumber == other._plateNumber && _row == other._row && _column == other._column;
  }
  
  public int hashCode()
  {
    return _plateNumber * (Well.MAX_WELL_COLUMN * Well.MAX_WELL_ROW) + _row * Well.MAX_WELL_COLUMN + _column;
  }
  
  public String toString()
  {
    return String.format("%0" + Well.PLATE_NUMBER_LEN + "d:%s", _plateNumber, getWellName());
  }
  
  public int compareTo(Object o)
  {
    return toString().compareTo(((WellKey) o).toString());
  }

  public String getWellName()
  {
    return String.format("%c%02d", 'A' + _row, _column + 1);
  }

  // private methods

}

