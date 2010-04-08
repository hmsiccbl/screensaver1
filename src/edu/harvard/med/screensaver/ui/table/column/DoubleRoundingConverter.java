// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;

import com.google.common.collect.Maps;

/**
 * Converts double values to/from string representations, showing a specific
 * number of decimal places when converting from a Double to a String, and
 * truncating extra decimal places when converting a String to a Double.
 * Half-even rounding is used in both cases when eliminating extra decimal
 * places. If the converter is configured with a negative value for the decimal
 * places argument, then the number of decimal places displayed/parsed will be
 * unaffacted.
 * 
 * @author atolopko
 */
public class DoubleRoundingConverter implements Converter
{
  private int _decimalPlaces;

  public DoubleRoundingConverter(int decimalPlaces)
  {
    _decimalPlaces = decimalPlaces;
  }

  @Override
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String n)
    throws ConverterException
  {
    if (StringUtils.isEmpty(n)) {
      return null;
    }
    return Double.valueOf(getAsString(arg0, arg1, new Double(n)));
  }

  @Override
  public String getAsString(FacesContext arg0, UIComponent arg1, Object n)
    throws ConverterException
  {
    if (n == null) {
      return "";
    }
    if (_decimalPlaces < 0) {
      return ((Double) n).toString();
    }
    return BigDecimal.valueOf((Double) n).setScale(_decimalPlaces, RoundingMode.HALF_EVEN).toPlainString();
  }
  
  private static Map<Integer,Converter> decimalPlacesConverters = Maps.newHashMap();

  public static Converter getInstance(Integer decimalPlaces)
  {
    if (decimalPlaces == null || decimalPlaces < 0) {
      decimalPlaces = -1;
    }

    if (!!!decimalPlacesConverters.containsKey(decimalPlaces)) {
      decimalPlacesConverters.put(decimalPlaces, new DoubleRoundingConverter(decimalPlaces));
    }
    return decimalPlacesConverters.get(decimalPlaces);
  }

}
