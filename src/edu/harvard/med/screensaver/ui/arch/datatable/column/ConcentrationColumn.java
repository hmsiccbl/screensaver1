// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/ui/arch/datatable/column/VolumeColumn.java $
// $Id: VolumeColumn.java 4960 2010-11-08 14:53:52Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import edu.harvard.med.screensaver.model.Concentration;

public abstract class ConcentrationColumn<R> extends TableColumn<R,Concentration>
{
  public ConcentrationColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.CONCENTRATION, group);
  }
}
