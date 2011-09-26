// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;

public class LibraryContentsVersionManager
{
  private static final Logger log = Logger.getLogger(LibraryContentsVersionManager.class);
  
  private GenericEntityDAO _dao;
  private PlateUpdater _plateUpdater;
  
  /**
   * @motivation for CGLIB2
   */
  public LibraryContentsVersionManager() {}

  public LibraryContentsVersionManager(GenericEntityDAO dao, 
                               PlateUpdater plateUpdater)
  {
    _dao = dao;
    _plateUpdater = plateUpdater;
  }

  @Transactional
  public LibraryContentsVersion createNewContentsVersion(Library library,
                                                         AdministratorUser performedBy,
                                                         String loadingComments)
  {
    library = _dao.reloadEntity(library);
    performedBy = _dao.reloadEntity(performedBy);
    LibraryContentsVersion contentsVersion = library.createContentsVersion(performedBy);
    library.getLastUpdateActivityOfType(AdministrativeActivityType.LIBRARY_CONTENTS_LOADING).setComments(loadingComments);
    return contentsVersion;
  }

  @Transactional
  public LibraryContentsVersion releaseLibraryContentsVersion(LibraryContentsVersion lcv, AdministratorUser admin)
  {
    admin = _dao.reloadEntity(admin);
    if (!admin.isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      throw new OperationRestrictedException("releaseLibraryContentsVersion");
    }
    lcv = _dao.reloadEntity(lcv);
    Library library = lcv.getLibrary();
    lcv.release(new AdministrativeActivity(admin, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE), false);
    _dao.flush();
    _dao.clear();

    // batch by plate to use constant memory
    int n = 0;
    Map<String,Object> criteria = Maps.newHashMap();
    for (int p = library.getStartPlate(); p <= library.getEndPlate(); ++p) {
      List<Well> wells = _dao.findEntitiesByProperty(Well.class, "plateNumber", new Integer(p), false, Well.latestReleasedReagent);
      for (Well well : wells) {
        criteria.put(Reagent.libraryContentsVersion.getLeaf(), lcv);
        criteria.put(Reagent.well.getLeaf(), well);
        Reagent reagent = _dao.findEntityByProperties(Reagent.class, criteria);
        well.setLatestReleasedReagent(reagent);
        ++n;
      }
      _dao.flush();
      _dao.clear();
      log.debug("updated " + n + " well(s)");
    }
    
    log.info("update plate concentrations");
    _plateUpdater.updatePrimaryWellConcentrations(library); 
    log.info("updated plate concentrations");
    
    return lcv;
  }
}
