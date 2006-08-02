// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.File;
import java.util.List;

import javax.faces.application.FacesMessage;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresult.ScreenResultParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;


public class MainController extends AbstractController
{
  private static final String APPLICATION_TITLE = null;

  private static Logger log = Logger.getLogger(MainController.class);
  
  private DAO _dao;
  
  private LibraryController _libraryController;
  
  private ScreenResultParser _screenResultParser;
  private ScreenResultViewerController _screenResultViewerController;
  
  private String _libraryNamePattern;

  private ScreenResult _sample1ScreenResult;
  private ScreenResult _sample2ScreenResult;
  
  public void setDao(DAO dao) {
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
  
  public ScreenResultViewerController getScreenResultViewerController()
  {
    return _screenResultViewerController;
  }

  public void setScreenResultViewerController(
    ScreenResultViewerController screenResultViewrController)
  {
    _screenResultViewerController = screenResultViewrController;
  }

  public ScreenResultParser getScreenResultParser()
  {
    return _screenResultParser;
  }

  public void setScreenResultParser(ScreenResultParser screenResultParser)
  {
    _screenResultParser = screenResultParser;
  }
  
  private ScreenResult loadScreenResult(File metadataFile)
  {
    ScreenResult result = null;
//    File serializedFile = new File(metadataFile + ".serialized");
    try {
//      if (serializedFile.exists()) {
//        result = (ScreenResult) new ObjectInputStream(new FileInputStream(serializedFile)).readObject();
//      } 
//      else {
        result = _screenResultParser.parse(metadataFile);
//        new ObjectOutputStream(new FileOutputStream(serializedFile)).writeObject(result);
//      }
    }
    catch (Exception e) {
      log.error("could not load screen result " + metadataFile + ": " + e.getMessage());
    }
    return result;
  }

  public String viewSample1ScreenResult()
  {
    assert _screenResultParser != null : "screenResultParser property must be set";
    if (_sample1ScreenResult == null) {
      File metadataFile = new File("/home/ant/iccb/screen-result-input-data/115/115MetaData.xls");
      _sample1ScreenResult = loadScreenResult(metadataFile);
    }
    // TODO: remove this hack! here just to make some screen result data
    // available; works on one developer's machine in particular (and I'm not
    // naming names!)
    _screenResultViewerController.setScreenResult(_sample1ScreenResult);
    return SUCCESS_ACTION_RESULT;
  }
  
  public String viewSample2ScreenResult()
  {
    assert _screenResultParser != null : "screenResultParser property must be set";
    // TODO: remove this hack! here just to make some screen result data
    // available; works on one developer's machine in particular (and I'm not
    // naming names!)
    if (_sample2ScreenResult == null) {
      File metadatafile = new File("/home/ant/iccb/screen-result-input-data/119/119MetaData.xls");
      _sample2ScreenResult = loadScreenResult(metadatafile);
    }
    _screenResultViewerController.setScreenResult(_sample2ScreenResult);
    return SUCCESS_ACTION_RESULT;
  }
  
  public String createLibrary() {
    _libraryController.setLibrary(new Library(
      "libraryName",
      "shortName",
      LibraryType.DOS,
      1,
      2));
    _libraryController.setUsageMode("create");
    return SUCCESS_ACTION_RESULT;
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
      return REDISPLAY_PAGE_ACTION_RESULT; // return to same view
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
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }
  
  public String reset() {
    _libraryNamePattern = "";
    return "reset";
  }

}
