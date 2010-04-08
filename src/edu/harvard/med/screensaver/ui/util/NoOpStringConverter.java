// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Note: converts empty string to null object, and null object to empty string (to be consistent with other converters)
 * @author drew
 */
public class NoOpStringConverter implements Converter
{
  public final static Converter _instance = new NoOpStringConverter();

  public Object getAsObject(FacesContext context,
                            UIComponent component,
                            String value) throws ConverterException
  {
    if (value.length() ==0) {
      return null;
    }
    return value;
  }

  public String getAsString(FacesContext context,
                            UIComponent component,
                            Object value) throws ConverterException
  {
    if (value == null) {
      return "";
    }
    return value.toString();
  }

  public static Converter getInstance()
  {
    return _instance;
  }
}
