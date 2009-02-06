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

import org.apache.log4j.Logger;

/*package*/ abstract class Quantity<Q extends Quantity<Q,T>, T extends Enum<T> & QuantityUnit<T>>  // have to match the bounds for QuantityUnit
  implements Comparable<Q>
{
  private static Logger log = Logger.getLogger(Quantity.class);

  private T _unit;
  private BigDecimal _value;
  private BigDecimal _displayValue;

  protected abstract Q newQuantity(BigDecimal value, T unit);

  public Quantity(Integer value, T unit)
  {
    this(value.toString(), unit);
  }

  public Quantity(Long value, T unit)
  {
    this(value.toString(), unit);
  }

  /**
   * Create a Quantity using a String representation of a decimal number, provided
   * in the specified units. If value has a fractional value, converts to units
   * that are small enough to hold the provided value as a whole number.
   *
   * @param value
   * @param unit
   */
  public Quantity(String value, T unit)
  {
    this(new BigDecimal(value), unit);
  }

  public Quantity(BigDecimal value, T unit)
  {
    _value = scaleValue(value, unit);
    _unit = unit;
  }

  /**
   * Return a Quantity scaled to the specified units. Protects against loss of
   * precision during conversion.
   *
   * @throws ArithmeticException if non-zero trailing decimal places would be
   *           lost in the conversion
   * @return a Quantity, scaled to the specified units
   */
  public Q convert(T newUnit)
  {
    if (_unit == newUnit) {
      return (Q) this;
    }
    return newQuantity(convertUnits((Q) this, newUnit)/*.toString()*/, newUnit);
  }

  /**
   * Return a Quantity scaled to the smallest units that can represent the value
   * as a whole number.
   */
  public Q convertToReasonableUnits()
  {
    Q newQuantity = newQuantity(_value, _unit);
    for (T newUnit : _unit.getValues()) {
      newQuantity._value = convertUnits(newQuantity, newUnit);
      newQuantity._unit = newUnit;
      if (newQuantity._value.compareTo(BigDecimal.ONE) >= 0) {
        break;
      }
    }
    newQuantity._value = scaleValue(newQuantity._value, newQuantity._unit);
    return newQuantity;
  }

  protected BigDecimal scaleValue(BigDecimal value, QuantityUnit<T> unit)
  {
    return value.setScale(unit.getValues()[unit.getValues().length - 1].getScale() - unit.getScale(),
                          RoundingMode.UNNECESSARY);
  }

  protected BigDecimal convertUnits(Q q, T newUnits)
  {
    return convertUnits(q.getValue(), q.getUnits().getScale(), newUnits.getScale());
  }

  private BigDecimal convertUnits(BigDecimal value, int oldScale, int newScale)
  {
    int scaleDelta = newScale - oldScale;
    return value.scaleByPowerOfTen(scaleDelta);
  }

  public Q add(Q value)
  {
    return newQuantity(_value.add(value.convert(_unit)._value), _unit);
  }

  public Q subtract(Q value)
  {
    return newQuantity(_value.subtract(value.convert(_unit)._value), _unit);
  }

  public Q negate()
  {
    return newQuantity(_value.negate()/*.toString()*/, _unit);
  }

  public T getUnits()
  {
    return _unit;
  }

  public BigDecimal getValue()
  {
    return _value;
  }

  public BigDecimal getValue(T units)
  {
    return convertUnits((Q) this, units);
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
    } while (scale <= _unit.getValues()[ _unit.getValues().length - 1].getScale());
    return value;
  }

  public BigDecimal getDisplayValue()
  {
    if (_displayValue == null) {
      _displayValue = stripTrailingFractionalZeros(_value);
    }
    return _displayValue;
  }

  @Override
  public String toString()
  {
    if (_displayValue == null) {
      _displayValue = stripTrailingFractionalZeros(_value);
    }
    return _displayValue.toString() + " " + _unit.getSymbol();
  }


  public int compareTo(Q o)
  {
    return _value.compareTo(o.convert(_unit)._value);
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
    if (!(obj instanceof Quantity)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) return false;
    Q other = (Q) obj;

    if (_value.equals( other._value ) && _unit == other._unit) return true;

    return (this._value.equals(convertUnits(other.getValue(), other.getUnits().getScale(), _unit.getScale())));
  }

  @Override
  public int hashCode()
  {
    return _value.hashCode() * 7 + _unit.hashCode() * 11;
  }

  // private methods

  protected boolean isFractional()
  {
    return !_value.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO.setScale(_value.scale()));
  }
}
