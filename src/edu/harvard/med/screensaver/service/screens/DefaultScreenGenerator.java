// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.screens.ScreenGenerator;

public class DefaultScreenGenerator implements ScreenGenerator
{
  private ScreenFacilityIdInitializer _screenIdentiferGenerator;
  private GenericEntityDAO _dao;

  protected DefaultScreenGenerator()
  {}

  public DefaultScreenGenerator(ScreenFacilityIdInitializer screenIdentiferGenerator,
                                GenericEntityDAO dao)

  {
    _screenIdentiferGenerator = screenIdentiferGenerator;
    _dao = dao;
  }

  @Override
  public Screen createPrimaryScreen(AdministratorUser admin, ScreeningRoomUser leadScreener, ScreenType screenType)
  {
    leadScreener = _dao.reloadEntity(leadScreener);

    Screen screen = new Screen(admin);
    screen.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    screen.setStudyType(StudyType.IN_VITRO);
    screen.setScreenType(screenType);
    if (leadScreener != null) {
      screen.setLeadScreener(leadScreener);
      screen.setLabHead(leadScreener.getLab().getLabHead());
    }
    _screenIdentiferGenerator.initializeFacilityId(screen);
    return screen;
  }

  @Override
  @Transactional
  public Screen createRelatedScreen(AdministratorUser admin, Screen primaryScreen, ProjectPhase projectPhase)
  {
    primaryScreen = _dao.reloadEntity(primaryScreen);

    Screen screen = new Screen(admin);
    screen.setTitle(primaryScreen.getTitle());
    screen.setSummary(primaryScreen.getSummary());
    screen.setProjectId(primaryScreen.getProjectId());
    screen.setProjectPhase(projectPhase);
    screen.setScreenType(primaryScreen.getScreenType());
    screen.setStudyType(primaryScreen.getStudyType());
    screen.setLeadScreener(primaryScreen.getLeadScreener());
    screen.setLabHead(primaryScreen.getLabHead());
    _screenIdentiferGenerator.initializeFacilityId(screen);
    return screen;
  }
}
