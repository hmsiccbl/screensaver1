// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import edu.harvard.med.screensaver.model.Volume;

public abstract class VolumeColumn<R> extends TableColumn<R, Volume>
{
  public VolumeColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.VOLUME, group);
  }
}
