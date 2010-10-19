// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.screens.ScreenGenerator;

public class DefaultScreenGenerator implements ScreenGenerator
{
  private ScreenIdentifierGenerator _screenIdentiferGenerator;

  protected DefaultScreenGenerator()
  {}

  public DefaultScreenGenerator(ScreenIdentifierGenerator screenIdentiferGenerator)
  {
    _screenIdentiferGenerator = screenIdentiferGenerator;
  }

  @Override
  public Screen create(AdministratorUser admin, ScreeningRoomUser leadScreener, ScreenType screenType)
  {
    Screen screen = new Screen(admin);
    if (leadScreener != null) {
      screen.setLeadScreener(leadScreener);
      screen.setLabHead(leadScreener.getLab().getLabHead());
    }
    screen.setScreenType(screenType);
    _screenIdentiferGenerator.updateIdentifier(screen);
    return screen;
  }
}
