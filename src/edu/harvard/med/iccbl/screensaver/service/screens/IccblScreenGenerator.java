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
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.screens.ScreenIdentifierGenerator;
import edu.harvard.med.screensaver.ui.screens.ScreenGenerator;

public class IccblScreenGenerator implements ScreenGenerator
{
  private ScreenIdentifierGenerator _screenIdentiferGenerator;
  private GenericEntityDAO _dao;

  protected IccblScreenGenerator()
  {}

  public IccblScreenGenerator(ScreenIdentifierGenerator screenIdentiferGenerator,
                              GenericEntityDAO dao)
  {
    _screenIdentiferGenerator = screenIdentiferGenerator;
    _dao = dao;
  }

  @Override
  @Transactional
  public Screen create(AdministratorUser admin,
                       ScreeningRoomUser leadScreener,
                       ScreenType screenType)
  {
    Screen screen = new Screen(admin);
    screen.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    screen.setStudyType(StudyType.IN_VITRO);
    leadScreener = _dao.reloadEntity(leadScreener);
    if (leadScreener != null) {
      screen.setLeadScreener(leadScreener);
      screen.setLabHead(leadScreener.getLab().getLabHead());
      if (screen.getLabHead() != null) {
        screen.setDataSharingLevel(DataSharingLevelMapper.getScreenDataSharingLevelForUser(screen.getScreenType(), screen.getLabHead()));
      }
    }
    // infer appropriate screen type from user's facility usage roles
    if (screenType == null && leadScreener != null) {
      screenType = leadScreener.isRnaiUser() && !leadScreener.isSmallMoleculeUser() ? ScreenType.RNAI
        : !leadScreener.isRnaiUser() && leadScreener.isSmallMoleculeUser() ? ScreenType.SMALL_MOLECULE : null;
    }
    screen.setScreenType(screenType);
    _screenIdentiferGenerator.updateIdentifier(screen);
    return screen;
  }
}
