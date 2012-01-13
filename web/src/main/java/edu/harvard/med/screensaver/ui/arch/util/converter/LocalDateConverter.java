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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class LocalDateConverter implements Converter
{

  private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
  private static final DateTimeFormatter parser = 
    new DateTimeFormatterBuilder().
    append(DateTimeFormat.forPattern("MM/dd")).
    appendLiteral("/").appendTwoDigitYear(1980, true).
    toFormatter();

  public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
    throws ConverterException
  {
    if ( arg2 == null || arg2.length() == 0) {
      return null;
    }
    try {
      return parser.parseDateTime(arg2).toLocalDate();
    }
    catch (Exception e) {
      throw new ConverterException("invalid date format: " + e.getMessage());
    }
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
    throws ConverterException
  {
    if (arg2 == null) {
      return "";
    }
    if (arg2 instanceof LocalDate) {
      return ((LocalDate) arg2).toString(formatter);
    }
    else if (arg2 instanceof DateTime) {
      return ((DateTime) arg2).toString(formatter);
    }
    else {
      return arg2.toString();
    }
  }
}
