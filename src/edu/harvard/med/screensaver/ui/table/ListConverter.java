// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.util.StringUtils;

public class ListConverter implements Converter
{
  private static final String DELIMITER = ";";
  private static final String DELIMITER_WITH_WHITESPACE = DELIMITER + " ";

  public Object getAsObject(FacesContext context,
                            UIComponent component,
                            String value) throws ConverterException
  {
    if (value == null) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<String>();
    for (String element : value.split(DELIMITER)) {
      result.add(element.trim());
    }
    return result;
  }

  public String getAsString(FacesContext context,
                            UIComponent component,
                            Object value) throws ConverterException
  {
    if (value == null) {
      return "";
    }
    return StringUtils.makeListString((List<?>) value, DELIMITER_WITH_WHITESPACE);
  }
}
