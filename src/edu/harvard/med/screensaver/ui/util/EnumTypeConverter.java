// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.model.libraries.LibraryType;

/**
 * A generic class that can convert between an Enum type's string values and
 * enum values. To use in a JSF application, extend this type and add a
 * constructor as follows:
 * 
 * <pre>
 * public MyEnumTypeConverter()
 * {
 *   super(EnumSet.allOf(MyEnumType.class));
 * }
 * </pre>
 * 
 * Now you can use MyEnumTypeConverter within your faces-config.xml file and in
 * your JSF JSP pages!
 * <p>
 * Note: It would have been nice to simply specify realized generic types such
 * as EnumTypeConverter&lt;MyEnumType&gt;, but I don't think we can specify
 * generic types in xml files; additionally, I have yet to figure out how to
 * implement such a generic class, which is parameterized on an Enum type, since
 * E.values() doesn't seem to be made available.
 * 
 * @author ant
 */
public class EnumTypeConverter<E extends Enum<E>> implements Converter
{
  
  private static class NormalizedString
  {
    private String _normalized;
    private String _removeMatchingRegex;
    private Pattern _pattern;
    
    public NormalizedString(String s)
    {
      this(s, "\\p{Punct}|\\p{Space}");
    }
    
    public NormalizedString(String s, String removeMatchingRegex)
    {
      _pattern = Pattern.compile(removeMatchingRegex);
      _normalized = normalize(s);
    }
    
    private String normalize(String s)
    {
      String normalizedString = _pattern.matcher(s.toLowerCase())
                                        .replaceAll("");
      return normalizedString;
    }
    
    @Override
    public boolean equals(Object o)
    {
      return normalize(o.toString()).equals(_normalized);
    }
    
    @Override
    public int hashCode()
    {
      return _normalized.hashCode();
    }
    
    public String toString()
    {
      return _normalized;
    }
  }
  
  private Map<NormalizedString,E> string2Enum = new HashMap<NormalizedString,E>();
  
  public EnumTypeConverter(EnumSet<E> enumSet)
  {
    for (E enumValue : enumSet) {
      string2Enum.put(new NormalizedString(enumValue.toString()),
                      enumValue);
    }
  }

  public Object getAsObject(
    FacesContext facesContext,
    UIComponent uiComponent,
    String stringValue) throws ConverterException
  {
    NormalizedString normalized = new NormalizedString(stringValue);
    if (!string2Enum.containsKey(normalized)) {
      throw new ConverterException("no matching object value for string '" + stringValue + "', normalized as '" + normalized + "'");
    }
    return string2Enum.get(normalized);
  }

  public String getAsString(
    FacesContext facesContext,
    UIComponent uiComponent,
    Object objectValue) 
    throws ConverterException
  {
    return ((LibraryType) objectValue).getValue();
  }

}
