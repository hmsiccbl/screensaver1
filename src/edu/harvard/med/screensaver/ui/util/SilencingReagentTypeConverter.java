// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;

public class SilencingReagentTypeConverter extends VocabularlyConverter<SilencingReagentType>
{
  public SilencingReagentTypeConverter()
  {
    super(SilencingReagentType.values());
  }
}
