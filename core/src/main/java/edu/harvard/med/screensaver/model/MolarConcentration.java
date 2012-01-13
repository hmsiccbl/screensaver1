// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.harvard.med.screensaver.util.StringUtils;

/**
 * A Concentration, in either millimolar, micromolar or nanomolar units, with
 * 1-nanoliter resolution (maximum).
 */
public class MolarConcentration extends Quantity<MolarConcentration,MolarUnit>
    {
  public MolarConcentration(Integer value)
  {
    super(value, MolarUnit.MILLIMOLAR);
  }

  public MolarConcentration(Integer value, MolarUnit units)
  {
    super(value.toString(), units);
  }

  public MolarConcentration(Long value)
  {
    this(value, MolarUnit.MILLIMOLAR);
  }

  public MolarConcentration(Long value, MolarUnit units)
  {
    super(value.toString(), units);
  }

  /**
   * Create a Concentration using a String representation of a decimal number, provided
   * in the specified units. If value has a fractional value, converts to units
   * that are small enough to hold the provided value as a whole number.
   * 
   * @param value
   * @param units
   */
  public MolarConcentration(String value, MolarUnit units)
  {
    super(new BigDecimal(value), units);
  }

  public MolarConcentration(String value, MolarUnit units, RoundingMode roundingMode)
  {
    super(new BigDecimal(value), units, roundingMode);
  }

  public MolarConcentration(BigDecimal value, MolarUnit units)
  {
    super(value, units);
  }

  @Override
  protected MolarConcentration newQuantity(BigDecimal value,
                                      MolarUnit unit)
  {
    return new MolarConcentration(value, unit);
  }
  
  /**
   * @motivation for UI
   */
  public static MolarConcentration makeConcentration(String value, MolarUnit unit)
  {
    MolarConcentration c = null;
    if (!StringUtils.isEmpty(value)) {
      c = new MolarConcentration(value, unit).convertToReasonableUnits();
    }
    return c;
  }
  
  public static MolarConcentration makeConcentration(String value, MolarUnit unit, RoundingMode roundingMode)
  {
    MolarConcentration c = null;
    if (!StringUtils.isEmpty(value)) {
      c = new MolarConcentration(value, unit, roundingMode).convertToReasonableUnits();
    }
    return c;
  }
}
