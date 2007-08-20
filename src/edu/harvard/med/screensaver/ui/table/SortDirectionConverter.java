// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.EnumSet;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.util.EnumTypeConverter;

public class SortDirectionConverter extends EnumTypeConverter<SortDirection>
{
  public SortDirectionConverter()
  {
    super(EnumSet.allOf(SortDirection.class));
  }
}
