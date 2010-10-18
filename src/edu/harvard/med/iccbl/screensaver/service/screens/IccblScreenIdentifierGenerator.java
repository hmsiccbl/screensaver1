// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screens.ScreenIdentifierGenerator;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.util.DevelopmentException;

public class IccblScreenIdentifierGenerator implements ScreenIdentifierGenerator
{
  private static final Logger log = Logger.getLogger(IccblScreenIdentifierGenerator.class);
  
  private static final Map<ProjectPhase,String> PROJECT_PHASE_FACILITY_ID_SUFFIX =
    ImmutableMap.of(ProjectPhase.FOLLOW_UP_SCREEN, "F",
                    ProjectPhase.COUNTER_SCREEN, "C",
                    ProjectPhase.ANNOTATION, "A");

  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;

  public IccblScreenIdentifierGenerator(GenericEntityDAO dao,
                                        ScreenDAO screenDao)
  {
    _dao = dao;
    _screenDao = screenDao;
  }

  @Override
  public boolean updateIdentifier(Screen screen)
  {
    if (screen.getFacilityId() != null) {
      return false;
    }
    if (screen.getProjectPhase() == ProjectPhase.PRIMARY_SCREEN) {
      return updateIdentifierForPrimaryScreen(screen);
    }
    return updateIdentifierForNonPrimaryScreen(screen);
  }

  private boolean updateIdentifierForNonPrimaryScreen(Screen screen)
  {
    Screen primaryScreen = _screenDao.findPrimaryScreen(screen);
    if (primaryScreen == null) {
      log.warn("cannot generate facility ID for screen " + screen + ": no primary screen");
      return false;
    }
    if (!PROJECT_PHASE_FACILITY_ID_SUFFIX.containsKey(screen.getProjectPhase())) {
      throw new DevelopmentException("unhandled project phase " + screen.getProjectPhase());
    }
    screen.setFacilityId(primaryScreen.getFacilityId() + PROJECT_PHASE_FACILITY_ID_SUFFIX.get(screen.getProjectPhase()));
    return true;
  }

  public boolean updateIdentifierForPrimaryScreen(Screen screen)
  {
    Integer lastPrimaryScreenFacilityId = _dao.runQuery(new Query<Integer>() {
      @Override
      public List<Integer> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.selectExpression("max(cast(s.facilityId as integer))").from(Screen.class, "s").
          where("s", "projectPhase", Operator.EQUAL, ProjectPhase.PRIMARY_SCREEN);
        return hql.toQuery(session, true).list();
      }
    }).get(0);

    String nextPrimaryScreenFacilityIdentifier = Integer.valueOf(lastPrimaryScreenFacilityId + 1).toString();
    screen.setFacilityId(nextPrimaryScreenFacilityIdentifier);
    log.info("set new primary screen facility ID to " + nextPrimaryScreenFacilityIdentifier);
    return true;
  }
}
