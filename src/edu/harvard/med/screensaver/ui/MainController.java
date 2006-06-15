// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.List;

import javax.faces.application.FacesMessage;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;

import org.apache.log4j.Logger;


public class MainController extends AbstractController
{
  private static Logger log = Logger.getLogger(MainController.class);
  
  private DAO _dao;
  
  private LibraryController _libraryController;
  
  private String _libraryNamePattern;
  
  public void setDAO(DAO dao) {
    _dao = dao;
  }

  public void setLibraryController(LibraryController libraryController) {
    _libraryController = libraryController;
  }
  
  public String getLibraryName() {
    return _libraryNamePattern;
  }

  public void setLibraryName(String libraryName) {
    _libraryNamePattern = libraryName;
  }
  
  public String createLibrary() {
    _libraryController.setLibrary(new Library(
      "libraryName",
      "shortName",
      LibraryType.DOS,
      1,
      2));
    _libraryController.setUsageMode("create");
    return "success";
  }
  
  public String findLibrary() {
    assert _dao != null : "dao property was not initialized";
    // note: we're doing the search now, rather than when the next view is invoked (is this "wrong"?)
    List<Library> librariesFound = _dao.findEntitiesByPropertyPattern(
        Library.class,
        "libraryName",
        _libraryNamePattern);
    if (librariesFound.size() == 0) {
      FacesMessage msg =
        getMessages().setFacesMessageForComponent("libraryPatternNotFound",
                                                  new Object[] {_libraryNamePattern},
                                                  "queryForm");
      log.debug(msg.getDetail());
      return null; // return to same view
    }
    else if (librariesFound.size() == 1) {
      _libraryController.setLibrary(librariesFound.get(0));
      _libraryController.setUsageMode("edit");
      log.debug("found single matching library '" + _libraryNamePattern + "'");
      return "found";
    }
    else {
      FacesMessage msg =
        getMessages().setFacesMessageForComponent("multipleLibrariesFoundForPattern",
                                                  new Object[] {_libraryNamePattern},
                                                  "queryForm");
      log.debug(msg.getDetail());
      // TODO: show all libraries in a table
      // _libraryListController.setLibraries(librariesFound);
      // return "found many";
      return null;
    }
  }
  
  public String reset() {
    _libraryNamePattern = "";
    return "reset";
  }
}
