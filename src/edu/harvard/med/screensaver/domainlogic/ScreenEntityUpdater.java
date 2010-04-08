// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.springframework.transaction.annotation.Transactional;

public class ScreenEntityUpdater implements EntityUpdater
{
  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;
  
  
  /** for CGLIB2 */
  protected ScreenEntityUpdater()
  {
  }

  public ScreenEntityUpdater(GenericEntityDAO dao,
                             ScreenDAO screenDao)
  {
    _dao = dao;
    _screenDao = screenDao;
  }

  @Override
  @Transactional
  public void apply(Entity entity)
  {
    _dao.flush();
    Screen screen = (Screen) entity;
    screen.setScreenedExperimentalWellCount(_screenDao.countScreenedExperimentalWells(screen, false));
    screen.setUniqueScreenedExperimentalWellCount(_screenDao.countScreenedExperimentalWells(screen, true));
    _dao.saveOrUpdateEntity(screen);
  }

  @Override
  public Class<? extends Entity> getEntityClass()
  {
    return Screen.class;
  }
}
