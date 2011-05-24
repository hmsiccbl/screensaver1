// $HeadURL:  $
// $Id: VolumeConverter.java 4960 2010-11-08 14:53:52Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;


public class BigDecimalConverter implements Converter
{
  private int _decimalPlaces;

  public BigDecimalConverter(int decimalPlaces)
  {
    _decimalPlaces = decimalPlaces;
  }

  /**
   * NOTE: input method is not used at this time
   */
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String s)
    throws ConverterException
  {
    if (StringUtils.isEmpty(s)) {
      return null;
    }
    return new BigDecimal(s).setScale(_decimalPlaces, RoundingMode.UNNECESSARY);
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object value)
    throws ConverterException
  {
    if (value == null) {
      return "";
    }
    return ((BigDecimal) value).setScale(_decimalPlaces, RoundingMode.HALF_UP).toString();
  }
}
