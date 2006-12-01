// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.screenresults.HeatMapViewer;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;

/**
 * Controller bean for views managed by the screenresults package. When
 * navigating between views <i>within</i> the screenresults package, the "last"
 * screen and screenSearchResults are remembered by this controller, allowing the
 * views to be unencumbered with passing around these objects.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenResultsController extends AbstractUIController
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(ScreenResultsController.class);
  private static String VIEW_SCREEN_RESULT_IMPORT_ERRORS = "viewScreenResultImportErrors";
  private static String VIEW_SCREEN_RESULT = "viewScreenResult";
  
  
  // private instance fields

  private DAO _dao;
  private Messages _messages;
  private ScreensController _screensController;
  private LibrariesController _librariesController;
  private ScreenResultViewer _screenResultViewer;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
  private ScreenResultExporter _screenResultExporter;
  private Screen _lastScreen;
  private ScreenSearchResults _lastScreenSearchResults;

  
  // public getters and setters

  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void setMessages(Messages messages)
  {
    _messages = messages;
  }
  
  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }
  
  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
    _screenResultViewer.setScreenResultsController(this);
  }
  
  public void setHeatMapViewer(HeatMapViewer heatMapViewer) 
  {
    _heatMapViewer = heatMapViewer;
  }

  public void setScreenResultImporter(ScreenResultImporter screenResultImporter) 
  {
    _screenResultImporter = screenResultImporter;
    _screenResultImporter.setScreenResultsController(this);
  }

  public void setScreenResultExporter(ScreenResultExporter screenResultExporter) 
  {
    _screenResultExporter = screenResultExporter;
  }

  
  // public control methods
 
  /**
   * Call this method when navigating from a Screen to a ScreenResult
   */
  @UIControllerMethod
  public String viewScreenResult(Screen screen, ScreenSearchResults screenSearchResults)
  {
    _lastScreen = screen;
    _lastScreenSearchResults = screenSearchResults;
    
    ScreenResult screenResult = screen.getScreenResult();
    
    _screenResultImporter.setDao(_dao);
    _screenResultImporter.setMessages(_messages);
    _screenResultImporter.setScreen(screen);
    _screenResultImporter.setScreenResultParser(new ScreenResultParser(_dao));
    
    _screenResultViewer.setScreen(screen);
    _screenResultViewer.setDao(_dao);
    _screenResultImporter.setMessages(_messages);
    _screenResultViewer.setScreenResultExporter(_screenResultExporter);
    _screenResultViewer.setScreensController(_screensController);
    _screenResultViewer.setLibrariesController(_librariesController);
    _screenResultViewer.setScreenSearchResults(screenSearchResults);

    _heatMapViewer.setDao(_dao);
    _heatMapViewer.setScreenResult(screenResult);
    _heatMapViewer.setLibrariesController(_librariesController);
    
    return VIEW_SCREEN_RESULT; 
  }
  
  public String viewLastScreenResult()
  {
    return viewScreenResult(_lastScreen, _lastScreenSearchResults);
  }
  
  public String viewScreenResultImportErrors()
  {
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }
}

