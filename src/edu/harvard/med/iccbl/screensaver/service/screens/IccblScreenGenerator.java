// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.screens.DefaultScreenGenerator;
import edu.harvard.med.screensaver.service.screens.ScreenIdentifierGenerator;

public class IccblScreenGenerator extends DefaultScreenGenerator
{
  protected IccblScreenGenerator()
  {}

  public IccblScreenGenerator(ScreenIdentifierGenerator screenIdentiferGenerator,
                              GenericEntityDAO dao)
  {
    super(screenIdentiferGenerator, dao);
  }

  @Override
  @Transactional
  public Screen createPrimaryScreen(AdministratorUser admin,
                                    ScreeningRoomUser leadScreener,
                                    ScreenType screenType)
  {
    Screen screen = super.createPrimaryScreen(admin, leadScreener, screenType);
    if (screen.getLabHead() != null) {
      screen.setDataSharingLevel(DataSharingLevelMapper.getScreenDataSharingLevelForUser(screen.getScreenType(), screen.getLabHead()));
    }
    // infer appropriate screen type from user's facility usage roles
    if (screenType == null && leadScreener != null) {
      screenType = leadScreener.isRnaiUser() && !leadScreener.isSmallMoleculeUser() ? ScreenType.RNAI
        : !leadScreener.isRnaiUser() && leadScreener.isSmallMoleculeUser() ? ScreenType.SMALL_MOLECULE : null;
    }
    return screen;
  }

  @Override
  @Transactional
  public Screen createRelatedScreen(AdministratorUser admin, Screen primaryScreen, ProjectPhase projectPhase)
  {
    Screen screen = super.createRelatedScreen(admin, primaryScreen, projectPhase);
    screen.setDataSharingLevel(primaryScreen.getDataSharingLevel());
    return screen;
  }
}
