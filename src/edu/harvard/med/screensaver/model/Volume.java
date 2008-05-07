// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

/**
 * A volume, in either liter, milliter, microliter or nanoliter units, with
 * 1-nanoliter resolution (maximum).
 */
public class Volume implements Comparable<Volume>
{
  // static members

  public static enum Units {
    LITERS("L", 0),
    MILLILITERS("mL", 3),
    MICROLITERS("uL", 6),
    NANOLITERS("nL", 9);

    private String _symbol;
    private int _scale;

    private Units(String symbol, int scale)
    {
      _symbol = symbol;
      _scale = scale;
    }

    public String getSymbol()
    {
      return _symbol;
    }

    public int getScale()
    {
      return _scale;
    }
  };

  private static final long serialVersionUID = 1L;
  public static final Volume ZERO = new Volume(0);


  // instance data members

  private BigDecimal _value;
  private Units _units;
  private BigDecimal _displayValue;


  // public constructors and methods

  public Volume(Integer value)
  {
    this(value, Units.MICROLITERS);
  }

  public Volume(Integer value, Units units)
  {
    this(value.toString(), units);
  }

  public Volume(Long value)
  {
    this(value, Units.MICROLITERS);
  }

  public Volume(Long value, Units units)
  {
    this(value.toString(), units);
  }

  /**
   * Create a WellVolume using a String representation of a decimal number,
   * provided in the specified units. Changes smaller units small enough to hold
   * the provided decimal number as a whole number.
   *
   * @param value
   * @param units
   */
  public Volume(String value, Units units)
  {
    this(new BigDecimal(value), units);
  }

  public Volume(BigDecimal value, Units units)
  {
    _value = scaleValue(value, units);
    _units = units;
  }

  public Units getUnits()
  {
    return _units;
  }

  public BigDecimal getValue()
  {
    return _value;
  }

  public BigDecimal getValue(Units units)
  {
    return convertUnits(this, units);
  }

  /**
   * Convert to new units new units. Protects against loss of precision during
   * conversion.
   *
   * @throws ArithmeticException if non-zero trailing decimal places would be
   *           lost in the conversion
   * @return a new Volume instance, with the converted value in the requested units
   */
  public Volume convert(Units newUnits)
  {
    if (_units == newUnits) {
      return this;
    }
    return new Volume(convertUnits(this, newUnits).toString(), newUnits);
  }

  public Volume convertToReasonableUnits()
  {
    EnumSet<Units> unitsSet = EnumSet.allOf(Units.class);
    Volume newVolume = new Volume(_value, _units);
    for (Units newUnits : unitsSet) {
      newVolume._value = convertUnits(newVolume, newUnits);
      newVolume._units = newUnits;
      if (newVolume._value.compareTo(BigDecimal.ONE) >= 0) {
        break;
      }
    }
    newVolume._value = scaleValue(newVolume._value, newVolume._units);
    return newVolume;
  }

  @Override
  public String toString()
  {
    if (_displayValue == null) {
      _displayValue = stripTrailingFractionalZeros(_value);
    }
    return _displayValue.toString() + " " + _units._symbol;
  }

  private BigDecimal stripTrailingFractionalZeros(BigDecimal value)
  {
    int scale = 0;
    do {
      BigDecimal truncated = _value.setScale(scale, RoundingMode.DOWN);
      if (truncated.setScale(value.scale()).equals(value)) {
        return truncated;
      }
      ++scale;
    } while (scale <= Units.values()[Units.values().length - 1].getScale());
    return value;
  }

  public Volume add(Volume value)
  {
    return new Volume(_value.add(value.convert(_units)._value), _units);
  }

  public Volume subtract(Volume value)
  {
    return new Volume(_value.subtract(value.convert(_units)._value), _units);
  }

  public Volume negate()
  {
    return new Volume(_value.negate().toString(), _units);
  }


  // private static methods

  private static BigDecimal scaleValue(BigDecimal value, Units units)
  {
    return value.setScale(Units.values()[Units.values().length - 1].getScale() - units.getScale(),
                          RoundingMode.UNNECESSARY);
  }

  private static BigDecimal convertUnits(Volume volume, Units newUnits)
  {
    int scaleDelta = newUnits.getScale() - volume.getUnits().getScale();
    return volume._value.scaleByPowerOfTen(scaleDelta);
  }

  public int compareTo(Volume o)
  {
    return _value.compareTo(o.convert(_units)._value);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Volume)) {
      return false;
    }
    Volume other = (Volume) obj;
    return _value.equals(other.convert(_units)._value);
  }

  @Override
  public int hashCode()
  {
    return _value.hashCode() * 7 + _units.hashCode() * 11;
  }

  // private methods

  private boolean isFractional()
  {
    return !_value.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO.setScale(_value.scale()));
  }
}
