// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.harvard.med.screensaver.util.StringUtils;

/**
 * A volume, in either liter, milliliter, microliter or nanoliter units, with
 * 1-nanoliter resolution (maximum).
 */
public class Volume extends Quantity<Volume,VolumeUnit>
{
  
  /** @deprecated use a constructor that takes an explicit VolumeUnit argument, for safety */
  @Deprecated
  public Volume(Integer value)
  {
    super(value, VolumeUnit.MICROLITERS);
  }

  public Volume(Integer value, VolumeUnit units)
  {
    super(value.toString(), units);
  }

  /** @deprecated use a constructor that takes an explicit VolumeUnit argument, for safety */
  @Deprecated
  public Volume(Long value)
  {
    this(value, VolumeUnit.MICROLITERS);
  }

  public Volume(Long value, VolumeUnit units)
  {
    super(value.toString(), units);
  }

  /**
   * Create a Volume using a String representation of a decimal number, provided
   * in the specified units. If value has a fractional value, converts to units
   * that are small enough to hold the provided value as a whole number.
   * 
   * @param value
   * @param units
   */
  public Volume(String value, VolumeUnit units)
  {
    super(new BigDecimal(value), units);
  }

  public Volume(BigDecimal value, VolumeUnit units)
  {
    super(value, units);
  }

  public Volume(String value, VolumeUnit unit, RoundingMode roundingMode)
  {
    super(new BigDecimal(value), unit, roundingMode);
  }

  @Override
  protected Volume newQuantity(BigDecimal value,
                                      VolumeUnit unit)
  {
    return new Volume(value, unit);
  }
  
  public static Volume makeVolume(String value, VolumeUnit unit)
  {
    Volume v = null;
    if (!StringUtils.isEmpty(value)) {
      v = new Volume(value, unit).convertToReasonableUnits();
    }
    return v;
  }
  
  public static Volume makeVolume(String value, VolumeUnit unit, RoundingMode roundingMode)
  {
    Volume c = null;
    if (!StringUtils.isEmpty(value)) {
      c = new Volume(value, unit, roundingMode).convertToReasonableUnits();
    }
    return c;
  }
  
}