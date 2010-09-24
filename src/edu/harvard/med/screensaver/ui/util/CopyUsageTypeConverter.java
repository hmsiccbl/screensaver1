// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.0.0-dev/src/edu/harvard/med/screensaver/ui/util/ScreenTypeConverter.java $
// $Id: ScreenTypeConverter.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import edu.harvard.med.screensaver.model.libraries.CopyUsageType;

public class CopyUsageTypeConverter extends VocabularlyConverter<CopyUsageType>
{
  public CopyUsageTypeConverter()
  {
    super(CopyUsageType.values());
  }
}
