// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

import org.apache.log4j.Logger;

public class MyScreenSearchResults extends ScreenSearchResults
{
  // static members

  private static Logger log = Logger.getLogger(MyScreenSearchResults.class);


  // instance data members

  // public constructors and methods

  protected MyScreenSearchResults()
  {
  }

  public MyScreenSearchResults(ScreenViewer screenViewer, GenericEntityDAO dao)
  {
    super(screenViewer, dao);
  }

  @Override
  public void searchAllScreens()
  {
    ScreensaverUser user = getCurrentScreensaverUser().getScreensaverUser();
    Set<Integer> screens = Collections.emptySet();
    if (user instanceof ScreeningRoomUser) {
      screens.addAll(findScreenIds((ScreeningRoomUser) user));
    }
    initialize(new EntitySetDataFetcher<Screen,Integer>(Screen.class, screens, _dao));
  }


  // private methods

  private Set<Integer> findScreenIds(ScreeningRoomUser screener)
  {
    Set<Screen> screens = new HashSet<Screen>();
    screens.addAll(screener.getScreensHeaded());
    screens.addAll(screener.getScreensLed());
    screens.addAll(screener.getScreensCollaborated());
    if (screens.size() == 0) {
      showMessage("screens.noScreensForUser");
    }
    else {
      for (Iterator<Screen> iter = screens.iterator(); iter.hasNext();) {
        Screen screen = iter.next();
        // note: it would be odd if the data access policy restricted
        // access to the screens we've determined to be "my screens",
        // above, but we'll filter anyway, just to be safe.
        if (screen.isRestricted()) {
          iter.remove();
        }
      }
    }
    Set<Integer> screenIds = new HashSet<Integer>();
    for (Screen screen : screens) {
      screenIds.add(screen.getEntityId());
    }
    return screenIds;
  }
}
