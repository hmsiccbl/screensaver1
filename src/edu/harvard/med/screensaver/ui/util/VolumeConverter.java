// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/util/ScreenTypeConverter.java $
// $Id: ScreenTypeConverter.java 2033 2007-11-13 00:04:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.Volume.Units;

import com.google.common.base.Join;

public class VolumeConverter implements Converter
{
  
  private static Pattern volumeAndUnitsPattern = Pattern.compile("\\s*([0-9.]+)\\s*([mun]?[lL])?");
  
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String s)
    throws ConverterException
  {
    if (s == null || s.trim().length() == 0) {
      return null;
    }
    Matcher matcher = volumeAndUnitsPattern.matcher(s);
    if (matcher.matches()) {
      String valueStr = matcher.group(1);
      String unitsStr = matcher.group(2);
      Units units = parseUnits(arg0, arg1, unitsStr);
      return new Volume(valueStr, units).convertToReasonableUnits();
    }
    throw new ConverterException("invalid volume value");
  }

  private Units parseUnits(FacesContext arg0, UIComponent arg1, String unitsStr)
  {
    if (unitsStr != null) {
      for (Units units : Units.values()) {
        if (units.getSymbol().toLowerCase().equalsIgnoreCase(unitsStr)) {
          return units;
        }
      }
      throw new ConverterException("unknown units (valid units: " + Join.join(", ", Units.values()) + ")");
    }
    return Units.MICROLITERS;
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object obj)
    throws ConverterException
  {
    if (obj == null) {
      return "";
    }
    return ((Volume) obj).toString();
  }

}
