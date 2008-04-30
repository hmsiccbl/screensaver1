// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/util/StatusValueConverter.java $
// $Id: StatusValueConverter.java 2033 2007-11-13 00:04:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import edu.harvard.med.screensaver.model.screens.AssayProtocolType;

public class AssayProtocolTypeConverter extends VocabularlyConverter<AssayProtocolType>
{
  public AssayProtocolTypeConverter()
  {
    super(AssayProtocolType.values());
  }
}
