// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/table/ListConverter.java $
// $Id: ListConverter.java 2190 2008-02-19 16:07:21Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.google.common.base.Join;

/**
 * Converts between a String representation of a collection (delimited by
 * commas) and a collection. Each element of the collection is itself
 * converted, using the Converter provided at instantiation.
 */
public abstract class CollectionConverter<C extends Collection<T>,T> implements Converter
{
  private static final String DELIMITER = ",";
  private static final String DELIMITER_WITH_WHITESPACE = DELIMITER + " ";
  private Converter _elementConverter;

  abstract protected C makeCollection();
  
  protected CollectionConverter(Converter elementConverter) 
  {
    _elementConverter = elementConverter;
  }
  
  public Object getAsObject(FacesContext context,
                            UIComponent component,
                            String value) throws ConverterException
  {
    if (value == null) {
      return Collections.emptyList();
    }
    C result = makeCollection();
    for (String token : value.split(DELIMITER)) {
      T element = (T) _elementConverter.getAsObject(context, component, token.trim());
      if (element != null) {
        result.add(element);
      }
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
    
    List<String> elements = new ArrayList<String>();
    for (T element : (Iterable<T>) value) {
      if (element != null) {
        elements.add(_elementConverter.getAsString(context, component, element));
      }
    }
    return Join.join(DELIMITER_WITH_WHITESPACE, elements);
  }
}
