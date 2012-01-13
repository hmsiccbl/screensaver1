// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;

public class PubmedCentralIdConverter implements Converter
{
  private static final String PMCID_PREFIX = "PMC";

  public Object getAsObject(FacesContext arg0, UIComponent arg1, String value)
    throws ConverterException
  {
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    value = value.trim();
    if (value.toUpperCase().startsWith(PMCID_PREFIX)) {
      value = value.substring(3);
    }
    return Integer.parseInt(value);
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
    throws ConverterException
  {
    if (arg2 == null) {
      return "";
    }
    return PMCID_PREFIX + arg2.toString();
  }
}
