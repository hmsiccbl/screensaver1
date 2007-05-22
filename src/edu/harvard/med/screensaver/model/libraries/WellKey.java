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
  private static final Pattern keyPattern = Pattern.compile("(\\d+):(.*)");
  private static final String wellKeyFormat = "%0" + Well.PLATE_NUMBER_LEN + "d:%s";
  
  
  // instance data members
  
  private int _plateNumber;

  private transient WellName _wellName;
  private transient int _hashCode = -1;
  private transient String _asString;


  // public constructors and methods
  
  private WellKey()
  {
  }

  public WellKey(int plateNumber, int row, int column)
  {
    _plateNumber = plateNumber;
    _wellName = new WellName(row, column);
  }
  
  public WellKey(int plateNumber, String wellName)
  {
    this(plateNumber, new WellName(wellName));
  }
  
  public WellKey(String key)
  {
    setKey(key);
  }
  
  public WellKey(int plateNumber, WellName wellName)
  {
    _plateNumber = plateNumber;
    _wellName = wellName;
  }

  public String getKey()
  {
    return toString();
  }
  
  public int getColumn()
  {
    return _wellName.getColumnIndex();
  }

  public int getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(int plateNumber)
  {
    _plateNumber = plateNumber;
    resetDerivedValues();
  }

  public int getRow()
  {
    return _wellName.getRowIndex();
  }

  private void resetDerivedValues()
  {
    _hashCode = -1;
    _asString = null;
  }

  public boolean equals(Object o)
  {
    if (o == this) { 
      return true;
    }
    WellKey other = (WellKey) o;
    return _plateNumber == other._plateNumber && _wellName.equals(other._wellName);
  }
  
  public int hashCode()
  {
    if (_hashCode == -1) {
      _hashCode = _plateNumber * (Well.PLATE_ROWS * Well.PLATE_COLUMNS) + 
      _wellName.getRowIndex() * Well.PLATE_COLUMNS + 
      _wellName.getColumnIndex();
    }
    return _hashCode;
  }
  
  public String toString()
  {
    if (_asString == null) {
      _asString = String.format(wellKeyFormat, _plateNumber, getWellName());
    }
    return _asString;
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
    _wellName = new WellName(wellName);
    resetDerivedValues();
  }

  public String getWellName()
  {
    return _wellName.toString();
  }

  // private methods

  /**
   * @motivation for hibernate and constructor
   */
  private void setKey(String key)
  {
    Matcher matcher = keyPattern.matcher(key);
    if (matcher.matches()) {
      _plateNumber = Integer.parseInt(matcher.group(1));
      _wellName = new WellName(matcher.group(2));
    } 
    else {
      throw new IllegalArgumentException("invalid composite well key string '" + key + "'");
    }
    resetDerivedValues();
  }
  
}

