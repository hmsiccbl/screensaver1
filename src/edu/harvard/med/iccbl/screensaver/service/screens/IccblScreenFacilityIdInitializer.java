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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screens.ScreenFacilityIdInitializer;

public class IccblScreenFacilityIdInitializer implements ScreenFacilityIdInitializer
{
  private static final Logger log = Logger.getLogger(IccblScreenFacilityIdInitializer.class);
  
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

  private static final Predicate<Screen> ScreenWithFacilityId = new Predicate<Screen>() {
    @Override
    public boolean apply(Screen screen)
    {
      return !screen.isStudyOnly();
    }
  };

  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;

  public IccblScreenFacilityIdInitializer(GenericEntityDAO dao,
                                          ScreenDAO screenDao)
  {
    _dao = dao;
    _screenDao = screenDao;
  }

  @Override
  public boolean initializeFacilityId(Screen screen)
  {
    if (screen.getFacilityId() != null) {
      return false;
    }

    if (updateIdentifierForRelatedScreen(screen)) {
      return true;
    }
    return updateIdentifierForPrimaryScreen(screen);
  }

  private boolean updateIdentifierForRelatedScreen(Screen screen)
  {
    if (screen.getProjectId() == null) {
      return false;
    }
    List<Screen> relatedScreens = _screenDao.findRelatedScreens(screen);
    List<Screen> projectScreens = _dao.findEntitiesByProperty(Screen.class, "projectId", screen.getProjectId(), true);
    projectScreens.remove(screen);
    if (projectScreens.size() > 0) {
      screen.setFacilityId(NonStrictStringToInteger.apply(relatedScreens.get(0).getFacilityId()) + "-" + projectScreens.size());
      return true;
    }
    return false;
  }

  public boolean updateIdentifierForPrimaryScreen(Screen screen)
  {
    List<Screen> screens = _dao.findAllEntitiesOfType(Screen.class, true);
    Iterable<Integer> primaryScreenNumericFacilityIds =
      Iterables.filter(Iterables.transform(Iterables.filter(screens, ScreenWithFacilityId),
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
