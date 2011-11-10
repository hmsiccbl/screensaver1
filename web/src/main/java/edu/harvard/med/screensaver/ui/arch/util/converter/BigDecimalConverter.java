// $HeadURL: $
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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;

public class BigDecimalConverter implements Converter
{
  private Integer _scale;
  private Integer _precision;

  public BigDecimalConverter(Integer scale)
  {
    _scale = scale;
  }

  public BigDecimalConverter(Integer scale, Integer precision)
  {
    _scale = scale;
    _precision = precision;
  }

  public Object getAsObject(FacesContext arg0, UIComponent arg1, String s)
    throws ConverterException
  {
    if (StringUtils.isEmpty(s)) {
      return null;
    }
    try {
      BigDecimal d = new BigDecimal(s);
      BigDecimal lowerBound = new BigDecimal(1).movePointLeft(_scale);
      if (d.compareTo(lowerBound) < 0) {
        throw new ConverterException(new FacesMessage(arg1.getId() + ": Conversion exception: minimum precision allowed is " +
          lowerBound));
      }
      if (_precision != null) {
        BigDecimal upperBound = new BigDecimal(1).movePointRight(_precision - _scale).setScale(_scale);
        if (d.compareTo(upperBound) > 0) {
          throw new ConverterException(new FacesMessage(arg1.getId() + ": Conversion exception: out of range: (>=" +
            lowerBound + ", <" + upperBound + ") "));
        }
      }
      d = d.setScale(_scale, RoundingMode.UNNECESSARY);
      return d;
    }
    catch (NumberFormatException e) {
      throw new ConverterException(new FacesMessage(arg1.getId() + ": Input value must be a valid number. ", e.getMessage()), e);
    }
    catch (Exception e) {
      throw new ConverterException(new FacesMessage(arg1.getId() + ": Number conversion exception. ", e.getMessage()), e);
    }
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object value)
    throws ConverterException
  {
    if (value == null) {
      return "";
    }
    return ((BigDecimal) value).setScale(_scale, RoundingMode.HALF_UP).toString();
  }
}
