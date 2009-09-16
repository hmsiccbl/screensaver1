// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.SortedSet;

import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;

import org.joda.time.LocalDate;

public interface ChecklistItemsEntity
{

  SortedSet<ChecklistItemEvent> getChecklistItemEvents();

  SortedSet<ChecklistItemEvent> getChecklistItemEvents(ChecklistItem checklistItem);

  ChecklistItemEvent createChecklistItemActivationEvent(ChecklistItem checklistItem,
                                                        LocalDate datePerformed,
                                                        AdministrativeActivity entryActivity);

  ChecklistItemEvent createChecklistItemNotApplicableEvent(ChecklistItem checklistItem,
                                                           LocalDate datePerformed,
                                                           AdministrativeActivity entryActivity);

}
