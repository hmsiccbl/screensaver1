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

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
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
                            Reagent reagent,
                            WellViewer wellViewer,
                            LibraryViewer libraryViewer,
                            StructureImageProvider structureImageProvider)
  {
    List<NameValueTable> comboNameValueTables = new ArrayList<NameValueTable>();
    comboNameValueTables.add(new WellDetailsNameValueTable(well, reagent, libraryViewer));
    if (reagent != null) {
      if (!!!reagent.isRestricted()) {
        if (reagent instanceof SilencingReagent) {
          SilencingReagent silencingReagent = (SilencingReagent) reagent;
          if (wellViewer.isAllowedAccessToSilencingReagentSequence(well)) {
            comboNameValueTables.add(new SilencingReagentNameValueTable(silencingReagent));
          }
          comboNameValueTables.add(new GeneNameValueTable(silencingReagent.getFacilityGene()));
        }
        else if (reagent instanceof SmallMoleculeReagent) {
          comboNameValueTables.add(new CompoundNameValueTable(((SmallMoleculeReagent) reagent),
                                                              structureImageProvider));
        }
        else if (reagent instanceof NaturalProductReagent) {
          // no additional data to show
        }
      }
    }
    initializeComboNameValueTable(comboNameValueTables);
  }
}

