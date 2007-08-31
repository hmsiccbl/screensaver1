// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the Well Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellNameValueTable extends ComboNameValueTable
{
  private static Logger log = Logger.getLogger(WellNameValueTable.class);

  public WellNameValueTable(Well well,
                            WellViewer wellViewer,
                            LibraryViewer libraryViewer,
                            GeneViewer geneViewer,
                            CompoundViewer compoundViewer)
  {
    List<NameValueTable> comboNameValueTables = new ArrayList<NameValueTable>();
    comboNameValueTables.add(new WellDetailsNameValueTable(well, libraryViewer));
    for (Gene gene : well.getGenes()) {
      comboNameValueTables.add(new GeneNameValueTable(gene, geneViewer, wellViewer));
    }
    for (Compound compound : well.getOrderedCompounds()) {
      comboNameValueTables.add(new CompoundNameValueTable(compound, compoundViewer, wellViewer));
    }
    initializeComboNameValueTable((NameValueTable [])
      comboNameValueTables.toArray(new NameValueTable [0]));
  }
}

