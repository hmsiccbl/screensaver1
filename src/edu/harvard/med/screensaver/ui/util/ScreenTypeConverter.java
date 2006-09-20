// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.EnumSet;

import edu.harvard.med.screensaver.model.screens.ScreenType;

public class ScreenTypeConverter extends EnumTypeConverter<ScreenType>
{
  public ScreenTypeConverter()
  {
    super(EnumSet.allOf(ScreenType.class));
  }
}
