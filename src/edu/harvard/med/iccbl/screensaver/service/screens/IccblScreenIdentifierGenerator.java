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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screens.ScreenIdentifierGenerator;

public class IccblScreenIdentifierGenerator implements ScreenIdentifierGenerator
{
  private static final Logger log = Logger.getLogger(IccblScreenIdentifierGenerator.class);
  
  private static final Map<ProjectPhase,String> PROJECT_PHASE_FACILITY_ID_SUFFIX =
    ImmutableMap.of(ProjectPhase.FOLLOW_UP_SCREEN, "F",
                    ProjectPhase.COUNTER_SCREEN, "C");

  private static Pattern numericPrefix = Pattern.compile("^([?0-9]+).*");
  private static final Function<String,Integer> NonStrictStringToInteger =
    new Function<String,Integer>() {
      @Override
      public Integer apply(String s)
      {
        try {
          Matcher m = numericPrefix.matcher(s);
          if (m.matches()) {
            return Integer.valueOf(m.group(1));
          }
        }
        catch (NumberFormatException e) {
        }
      return null;
      }
    };

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
      return false;
    }
    screen.setFacilityId(primaryScreen.getFacilityId() + "-" + PROJECT_PHASE_FACILITY_ID_SUFFIX.get(screen.getProjectPhase()));
    return true;
  }

  public boolean updateIdentifierForPrimaryScreen(Screen screen)
  {
    List<Screen> primaryScreens = _dao.findEntitiesByProperty(Screen.class,
                                                              "projectPhase",
                                                              ProjectPhase.PRIMARY_SCREEN,
                                                              true);
    Iterable<Integer> primaryScreenNumericFacilityIds =
      Iterables.filter(Iterables.transform(primaryScreens,
                                           Functions.compose(NonStrictStringToInteger, Screen.ToFacilityId)),
                       Predicates.notNull());

    
    int nextId = 1;
    if (primaryScreenNumericFacilityIds.iterator().hasNext()) {
      Integer maxNumericFacilityId = Ordering.natural().max(primaryScreenNumericFacilityIds);
      nextId += maxNumericFacilityId;
    }
    String nextPrimaryScreenFacilityIdentifier = Integer.toString(nextId);
    screen.setFacilityId(nextPrimaryScreenFacilityIdentifier);
    log.info("set new primary screen facility ID to " + nextPrimaryScreenFacilityIdentifier);
    return true;
  }
}
