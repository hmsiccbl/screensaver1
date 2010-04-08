// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.libraries.SilencingReagent;

/**
 * A NameValueTable for Silencing Reagents.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SilencingReagentNameValueTable extends ReagentNameValueTable<SilencingReagent>
{
  private static final String SEQUENCE = "Sequence";

  public SilencingReagentNameValueTable(SilencingReagent reagent)
  {
    super(reagent);
    initializeLists(reagent);
    setDataModel(new ListDataModel(_values));
  }
  
  @Override
  protected void initializeLists(SilencingReagent reagent)
  {
    super.initializeLists(reagent);
    addItem(SEQUENCE, reagent.getSequence(), ValueType.TEXT, "The nucleotide sequence of the silencing reagent");
  }
}

