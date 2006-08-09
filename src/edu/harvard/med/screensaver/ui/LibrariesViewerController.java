// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/LibraryViewerController.java $
// $Id: LibraryViewerController.java 443 2006-08-09 20:43:32Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Library;

public class LibrariesViewerController extends AbstractController
{
  private static Logger log = Logger.getLogger(LibrariesViewerController.class);
  
  private DAO _dao;
  private List<Library> _libraries;

  
  // property getter/setter methods
  
  public void setDao(DAO dao) {
    _dao = dao;
  }

  public List<Library> getLibraries() {
    if (_libraries == null) {
      initializeLibraries();
    }
    return _libraries;
  }

  
  // JSF Application methods

  
  // private instance methods
  
  private void initializeLibraries()
  {
    _libraries = _dao.findAllEntitiesWithType(Library.class);
  }
}
