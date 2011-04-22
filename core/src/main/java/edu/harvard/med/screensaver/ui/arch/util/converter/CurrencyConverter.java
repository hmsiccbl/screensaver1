// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;

public class CurrencyConverter implements Converter
{
  private static final Currency CURRENCY = Currency.getInstance(Locale.getDefault());
  private static final int CURRENCY_SCALE = CURRENCY.getDefaultFractionDigits();
  
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
    throws ConverterException
  {
    if (StringUtils.isEmpty(arg2)) {
      return null;
    }
    // TODO: should use RoundingMode.UNNECESSARY, but having a problem with MyFaces
    arg2 = arg2.replace(CURRENCY.getSymbol(), "");
    return new BigDecimal(arg2).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
    throws ConverterException
  {
    if (arg2 == null) {
      return "";
    }
    // TODO: should use RoundingMode.UNNECESSARY, but having a problem with MyFaces
    return CURRENCY.getSymbol() + ((BigDecimal) arg2).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP).toString();
  }
}
