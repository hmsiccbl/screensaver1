// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

public class LibraryContentsVersionManager
{
  private static final int BATCH_SIZE = 384;
  private static final Logger log = Logger.getLogger(LibraryContentsVersionManager.class);
  
  private GenericEntityDAO _dao;
  
  /**
   * @motivation for CGLIB2
   */
  public LibraryContentsVersionManager() {}

  public LibraryContentsVersionManager(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Transactional
  public LibraryContentsVersion createNewContentsVersion(Library library,
                                                         AdministratorUser performedBy,
                                                         String loadingComments)
  {
    library = _dao.reloadEntity(library);
    performedBy = _dao.reloadEntity(performedBy);
    AdministrativeActivity loadingActivity = 
      new AdministrativeActivity(performedBy,
                                 new LocalDate(),
                                 AdministrativeActivityType.LIBRARY_CONTENTS_LOADING);
    loadingActivity.setComments(loadingComments);
    LibraryContentsVersion contentsVersion = library.createContentsVersion(loadingActivity);
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
      List<Well> wells = _dao.findEntitiesByProperty(Well.class, "plateNumber", new Integer(p), false, Well.latestReleasedReagent.getPath());
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
    return lcv;
  }
}
