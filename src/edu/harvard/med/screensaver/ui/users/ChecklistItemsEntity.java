// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.SortedSet;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;

import org.joda.time.LocalDate;

public interface ChecklistItemsEntity extends Entity
{

  SortedSet<ChecklistItemEvent> getChecklistItemEvents();

  SortedSet<ChecklistItemEvent> getChecklistItemEvents(ChecklistItem checklistItem);

  ChecklistItemEvent createChecklistItemActivationEvent(ChecklistItem checklistItem,
                                                        LocalDate datePerformed,
                                                        AdministratorUser recordedBy);
                                                        
  ChecklistItemEvent createChecklistItemNotApplicableEvent(ChecklistItem checklistItem,
                                                           LocalDate datePerformed,
                                                           AdministratorUser recordedBy);

}
