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

import edu.harvard.med.screensaver.ui.arch.util.Messages;
import edu.harvard.med.screensaver.util.StringUtils;

public class BigDecimalConverter implements Converter
{
  private Integer _scale;
  private Integer _precision;
  private Messages _messages;

  public void setMessages(Messages messages)
  {
    _messages = messages;
  }
  
  public void setScale(Integer scale)
  {
    _scale = scale;
  }
  
  public void setPrecision(Integer precision)
  {
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
        // Note, this should be reworked using a Validator, however, see [#1259]
        // Note: this message will eventually be retrieved by the calling AbstractBackingBean class and enqued in the Messages (how?) - sde4
        throw new ConverterException(new FacesMessage(arg1.getId() +": " +  _messages.getMessage("conversionExceptionMinPrecisionAllowed", lowerBound), "")); 
      }
      if (_precision != null) {
        BigDecimal upperBound = new BigDecimal(1).movePointRight(_precision - _scale).setScale(_scale);
        if (d.compareTo(upperBound) > 0) {
          // Note, this should be reworked using a Validator, however, see [#1259]
          // Note: this message will eventually be retrieved by the calling AbstractBackingBean class and enqued in the Messages (how?) - sde4
          throw new ConverterException(new FacesMessage(arg1.getId() +": " +  _messages.getMessage("conversionExceptionPrecisionRangeAllowed", lowerBound, upperBound), "" ));
        }
      }
      d = d.setScale(_scale, RoundingMode.UNNECESSARY);
      return d;
    }
    catch(ConverterException e) {
      throw e;
    }
    catch (NumberFormatException e) {
      // Note, this should be reworked using a Validator, however, see [#1259]
      // Note: this message will eventually be retrieved by the calling AbstractBackingBean class and enqued in the Messages (how?) - sde4
      throw new ConverterException(new FacesMessage(arg1.getId() + ": Input value must be a valid number. ", e.getMessage()), e); //TODO: use messages
    }
    catch (Exception e) {
      // Note, this should be reworked using a Validator, however, see [#1259]
      // Note: this message will eventually be retrieved by the calling AbstractBackingBean class and enqued in the Messages (how?) - sde4
      throw new ConverterException(new FacesMessage(arg1.getId() + ": Number conversion exception. ", e.getMessage()), e); //TODO: use messages
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
