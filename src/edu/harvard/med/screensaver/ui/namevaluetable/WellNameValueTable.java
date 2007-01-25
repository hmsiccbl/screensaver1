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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

public class WellNameValueTable extends ComboNameValueTable
{
  private static Logger log = Logger.getLogger(WellNameValueTable.class);

  public WellNameValueTable(LibrariesController librariesController, Well well)
  {
    List<NameValueTable> comboNameValueTables = new ArrayList<NameValueTable>();
    comboNameValueTables.add(new WellDetailsNameValueTable(librariesController, well));
    for (Gene gene : well.getGenes()) {
      comboNameValueTables.add(new GeneNameValueTable(librariesController, gene));
    }
    for (Compound compound : well.getOrderedCompounds()) {
      comboNameValueTables.add(new CompoundNameValueTable(librariesController, compound));
    }
    initializeComboNameValueTable((NameValueTable [])
      comboNameValueTables.toArray(new NameValueTable [comboNameValueTables.size()]));
  }
}

