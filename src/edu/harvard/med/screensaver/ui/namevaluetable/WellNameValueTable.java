// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;

/**
 * A NameValueTable for the Well Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellNameValueTable extends ReagentNameValueTable
{
  private static Logger log = Logger.getLogger(WellNameValueTable.class);

  public WellNameValueTable(Well well,
                            WellViewer wellViewer,
                            LibraryViewer libraryViewer,
                            GeneViewer geneViewer,
                            CompoundViewer compoundViewer)
  {
    super(well, wellViewer, geneViewer, compoundViewer, new WellDetailsNameValueTable(well, libraryViewer));
  }
}

