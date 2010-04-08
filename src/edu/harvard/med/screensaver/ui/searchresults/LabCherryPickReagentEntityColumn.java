// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

public class LabCherryPickReagentEntityColumn<R extends Reagent,T> extends RelatedEntityColumn<LabCherryPick,R,T> 
{
  
  public LabCherryPickReagentEntityColumn(Class<R> reagentClass,
                                          TableColumn<R,T> delegateEntityColumn)
  {
    super(reagentClass, LabCherryPick.sourceWell.to(Well.latestReleasedReagent), delegateEntityColumn);
  }

  @Override
  protected R getRelatedEntity(LabCherryPick lcp)
  {
    return lcp.getSourceWell().<R>getLatestReleasedReagent();
  }
}

