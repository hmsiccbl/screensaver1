// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.ui.libraries.ReagentViewer;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the Reagent Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ReagentNameValueTable extends ComboNameValueTable
{
  private static Logger log = Logger.getLogger(ReagentNameValueTable.class);

  public ReagentNameValueTable(Well well,
                               ReagentViewer reagentViewer,
                               GeneViewer geneViewer,
                               CompoundViewer compoundViewer)
  {
    initialize(well, reagentViewer, geneViewer, compoundViewer, new ReagentDetailsNameValueTable(well));
  }

  protected ReagentNameValueTable(Well well,
                                  ReagentViewer reagentViewer,
                                  GeneViewer geneViewer,
                                  CompoundViewer compoundViewer,
                                  NameValueTable detailsNameValueTable)
  {
    initialize(well, reagentViewer, geneViewer, compoundViewer, detailsNameValueTable);
  }

  private void initialize(Well well,
                          ReagentViewer reagentViewer,
                          GeneViewer geneViewer,
                          CompoundViewer compoundViewer,
                          NameValueTable detailsNameValueTable)
  {
    List<NameValueTable> comboNameValueTables = new ArrayList<NameValueTable>();
    comboNameValueTables.add(detailsNameValueTable);
    for (Gene gene : well.getGenes()) {
      comboNameValueTables.add(new GeneNameValueTable(gene, geneViewer, reagentViewer));
    }
    for (Compound compound : well.getOrderedCompounds()) {
      comboNameValueTables.add(new CompoundNameValueTable(compound, compoundViewer, reagentViewer));
    }
    initializeComboNameValueTable((NameValueTable [])
      comboNameValueTables.toArray(new NameValueTable [0]));
  }

}
