// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.google.common.base.Joiner;

import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.ConcentrationUnit;

public class ConcentrationConverter implements Converter
{
  private static Pattern concentrationAndUnitsPattern = Pattern.compile("\\s*([0-9.]+)\\s*([munp]?[mM])?");

  public Object getAsObject(FacesContext arg0, UIComponent arg1, String s)
    throws ConverterException
  {
    if (s == null || s.trim().length() == 0) {
      return null;
    }
    Matcher matcher = concentrationAndUnitsPattern.matcher(s);
    if (matcher.matches()) {
      String valueStr = matcher.group(1);
      String unitsStr = matcher.group(2);
      ConcentrationUnit units = parseUnits(arg0, arg1, unitsStr);
      return new Concentration(valueStr, units).convertToReasonableUnits();
    }
    throw new ConverterException("invalid concentration value");
  }

  private ConcentrationUnit parseUnits(FacesContext arg0, UIComponent arg1, String unitsStr)
  {
    if (unitsStr != null) {
      for (ConcentrationUnit units : ConcentrationUnit.values()) {
        if (units.getSymbol().toLowerCase().equalsIgnoreCase(unitsStr)) {
          return units;
        }
      }
      throw new ConverterException("unknown units (valid units: " + Joiner.on(", ").join(ConcentrationUnit.values()) + ")");
    }
    return ConcentrationUnit.DEFAULT;
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object obj)
    throws ConverterException
  {
    if (obj == null) {
      return "";
    }
    return ((Concentration) obj).toString();
  }

}
