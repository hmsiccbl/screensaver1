// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/ui/arch/util/converter/VolumeConverter.java $
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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.util.StringUtils;


public class MgMlConcentrationConverter implements Converter
{
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String s)
    throws ConverterException
  {
    if (StringUtils.isEmpty(s)) {
      return null;
    }
    return new BigDecimal(s).setScale(ScreensaverConstants.MG_ML_CONCENTRATION_SCALE, RoundingMode.UNNECESSARY);
  }

  public String getAsString(FacesContext arg0, UIComponent arg1, Object value)
    throws ConverterException
  {
    if (value == null) {
      return "";
    }
    return value.toString();
  }
}
