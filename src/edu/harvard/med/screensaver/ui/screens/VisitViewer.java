// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.VisitType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;

import org.apache.log4j.Logger;

public class VisitViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger
    .getLogger(VisitViewer.class);


  // instance data members
  
  private Screen _screen;

  // public constructors and methods
  
  public List<SelectItem> getVisitTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(VisitType.values());
  }

  public List<SelectItem> getVisitPerformedBySelectItems()
  {
    // TODO: move the logic for determing potential collaborators to Screen
    List<SelectItem> visitPerformedBySelectItems = new ArrayList<SelectItem>();
    SortedSet<ScreeningRoomUser> performedByCandidates = 
      new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    performedByCandidates.addAll(_screen.getLabHead().getLabMembers());
    performedByCandidates.addAll(_screen.getCollaborators());
    for (ScreeningRoomUser performedBy : performedByCandidates) {
      visitPerformedBySelectItems.add(new SelectItem(performedBy, performedBy.getFullName()));
    }
    return visitPerformedBySelectItems;
  }
  

  // private methods

}

