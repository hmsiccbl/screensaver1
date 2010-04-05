// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;

import org.springframework.transaction.annotation.Transactional;

public class LibraryScreeningEntityUpdater implements EntityUpdater
{
  private LibrariesDAO _librariesDao;
  
  
  /** for CGLIB2 */
  protected LibraryScreeningEntityUpdater()
  {
  }

  public LibraryScreeningEntityUpdater(LibrariesDAO librariesDao)
  {
    _librariesDao = librariesDao;
  }

  @Override
  @Transactional
  public void apply(Entity entity)
  {
    LibraryScreening libraryScreening = (LibraryScreening) entity;
    int n = 0;
    for (PlatesUsed platesUsed : libraryScreening.getPlatesUsed()) {
      n += _librariesDao.countExperimentalWells(platesUsed.getStartPlate(), platesUsed.getEndPlate());
    }
    libraryScreening.setScreenedExperimentalWellCount(n);
  }

  @Override
  public Class<? extends Entity> getEntityClass()
  {
    return LibraryScreening.class;
  }
}
