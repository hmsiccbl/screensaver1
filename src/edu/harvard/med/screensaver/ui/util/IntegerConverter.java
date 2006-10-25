// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.log4j.Logger;

public class IntegerConverter implements Converter
{
  // static members

  private static Logger log = Logger.getLogger(IntegerConverter.class);

  // public constructors and methods

  public Object getAsObject(FacesContext context,
                            UIComponent component,
                            String value) throws ConverterException
  {
    if (value == null || value.length() == 0) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      throw new ConverterException(e.getMessage());
    }
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

}

