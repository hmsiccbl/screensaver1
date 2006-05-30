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
import javax.faces.context.FacesContext;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.db.LabDAO;

import org.apache.log4j.Logger;


public class MainController
{
  private static Logger log = Logger.getLogger(MainController.class);
  
  private LabDAO _labDAO;
  
  private LibraryController _libraryController;
  
  private String _libraryNamePattern;


  /* Property getter/setter methods */

  public void setLabDAO(LabDAO dao) {
    _labDAO = dao;
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
  
  
  /* JSF Application Methods */
  
  public String createLibrary() {
    _libraryController.setLibrary(new Library());
    _libraryController.setUsageMode("create");
    return "success";
  }
  
  public String findLibrary() {
    assert _labDAO != null : "labDAO property was not initialized";
    // note: we're doing the search now, rather than when the next view is invoked (is this "wrong"?)
    List<Library> librariesFound = _labDAO.findLibrariesWithMatchingName(_libraryNamePattern);
    if (librariesFound.size() == 0) {
      // TODO: set the library query field's message, if no result found (requires component instance binding?)
      log.debug("no library found matching '" + _libraryNamePattern + "'");
      FacesContext.getCurrentInstance().addMessage("queryForm", new FacesMessage("Found no libraries.  Please modify search pattern."));
      return null; // return to same view
    }
    else if (librariesFound.size() == 1) {
      _libraryController.setLibrary(librariesFound.get(0));
      _libraryController.setUsageMode("edit");
      log.debug("found single matching library '" + _libraryNamePattern + "'");
      return "found";
    }
    else {
      FacesContext.getCurrentInstance().addMessage("queryForm", new FacesMessage("Found multiple libraries.  Please refine search pattern."));
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
