// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.io.Serializable;
import java.util.SortedSet;

import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.Entity;

public interface ChecklistItemsEntity<K extends Serializable> extends Entity<K>
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
