// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.screenresults.HeatMapViewer;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenFinder;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.screens.ScreensBrowser;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreensController extends AbstractUIController
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(ScreensController.class);
  private static final String BROWSE_SCREENS = "browseScreens";
  private static final String VIEW_SCREEN = "viewScreen";
  private static String VIEW_SCREEN_RESULT_IMPORT_ERRORS = "viewScreenResultImportErrors";
  private static String VIEW_SCREEN_RESULT = "viewScreenResult";

  
  // private instance fields
  
  private DAO _dao;
  private LibrariesController _librariesController;
  private ScreensBrowser _screensBrowser;
  private ScreenViewer _screenViewer;
  private ScreenResultViewer _screenResultViewer;
  private ScreenFinder _screenFinder;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
  private ScreenResultExporter _screenResultExporter;
  private Screen _lastScreen;
  private ScreenSearchResults _lastScreenSearchResults;
  

  // public getters and setters
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }
  
  public ScreensBrowser getScreensBrowser()
  {
    return _screensBrowser;
  }
  
  public void setScreensBrowser(ScreensBrowser screensBrowser)
  {
    _screensBrowser = screensBrowser;
    _screensBrowser.setScreensController(this);
  }
  
  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
    _screenViewer.setScreensController(this);
  }
  
  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
    _screenResultViewer.setScreensController(this);
  }

  public void setScreenFinder(ScreenFinder screenFinder)
  {
    _screenFinder = screenFinder;
    _screenFinder.setScreensController(this);
  }

  public void setHeatMapViewer(HeatMapViewer heatMapViewer) 
  {
    _heatMapViewer = heatMapViewer;
  }

  public void setScreenResultImporter(ScreenResultImporter screenResultImporter) 
  {
    _screenResultImporter = screenResultImporter;
    _screenResultImporter.setScreensController(this);
  }

  public void setScreenResultExporter(ScreenResultExporter screenResultExporter) 
  {
    _screenResultExporter = screenResultExporter;
  }

  
  // public controller methods

  @UIControllerMethod
  public String browseScreens()
  {
    if (_screensBrowser.getScreenSearchResults() == null) {
      List<Screen> screens = _dao.findAllEntitiesWithType(Screen.class);
      _screensBrowser.setScreenSearchResults(new ScreenSearchResults(screens, this, _dao));
    }
    return BROWSE_SCREENS;
  }
  
  @UIControllerMethod
  public String viewScreen(Screen screen, ScreenSearchResults screenSearchResults)
  {
    _dao.persistEntity(screen); // re-attach to Hibernate session
    // force initialization
    Hibernate.initialize(screen.getAbaseTestsets());
    Hibernate.initialize(screen.getAssayReadoutTypes());
    Hibernate.initialize(screen.getHbnCollaborators());
    Hibernate.initialize(screen.getAttachedFiles());
    Hibernate.initialize(screen.getBillingInformation());
    Hibernate.initialize(screen.getFundingSupports());
    Hibernate.initialize(screen.getKeywords());
    Hibernate.initialize(screen.getLettersOfSupport());
    Hibernate.initialize(screen.getPublications());
    Hibernate.initialize(screen.getStatusItems());
    Hibernate.initialize(screen.getVisits());
    Hibernate.initialize(screen.getLabHead());
    Hibernate.initialize(screen.getLabHead().getLabMembers());
    Hibernate.initialize(screen.getLeadScreener());
    Hibernate.initialize(screen.getScreenResult());

    _screenViewer.setDao(_dao);
    _screenViewer.setScreen(screen);
    _screenViewer.setCandidateLabHeads(_dao.findAllLabHeads());
    _screenViewer.setCandidateCollaborators(_dao.findAllEntitiesWithType(ScreeningRoomUser.class));
    _screenViewer.setScreenSearchResults(screenSearchResults);

    initializeScreenResultBackingBeans(screen, screenSearchResults);

    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String saveScreen(Screen screen)
  {
    try {
      _dao.persistEntity(screen);
    }
    catch (Throwable e) {
      reportSystemError(e);
    }
    return VIEW_SCREEN;
  }

  public String deleteScreenResult(ScreenResult screenResult)
  {
    if (screenResult != null) {
      _dao.persistEntity(screenResult); // really, we just want to reattach to Hibernate session
      _dao.deleteScreenResult(screenResult);
    }
    return viewLastScreen();
  }

  
  // public control methods
 
  @UIControllerMethod
  public String viewLastScreen()
  {
    return viewScreen(_lastScreen, _lastScreenSearchResults);
  }
    
  @UIControllerMethod
  public String viewScreenResultImportErrors()
  {
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }

  @UIControllerMethod
  public String addStatusItem(Screen screen, StatusValue statusValue)
  {
    if (statusValue != null) {
      _dao.defineEntity(StatusItem.class,
                        screen,
                        new Date(),
                        statusValue);
      _screenViewer.setNewStatusValue(null);
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteStatusItem(Screen screen, StatusItem statusItem)
  {
    screen.getStatusItems().remove(statusItem);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addPublication(Screen screen)
  {
    _dao.defineEntity(Publication.class, screen, "<new>", "", "", "");
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deletePublication(Screen screen, Publication publication)
  {
    screen.getPublications().remove(publication);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addLetterOfSupport(Screen screen)
  {
    _dao.defineEntity(LetterOfSupport.class, screen, new Date(), "");
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteLetterOfSupport(Screen screen, LetterOfSupport letterOfSupport)
  {
    screen.getLettersOfSupport().remove(letterOfSupport);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addAttachedFile(Screen screen)
  {
    _dao.defineEntity(AttachedFile.class, screen, "<new>", "");
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteAttachedFile(Screen screen, AttachedFile attachedFile)
  {
    screen.getAttachedFiles().remove(attachedFile);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    if (fundingSupport != null) {
      screen.addFundingSupport(fundingSupport);
      _screenViewer.setNewFundingSupport(null);
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    screen.getFundingSupports().remove(fundingSupport);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addAbaseTestset(Screen screen)
  {
    _dao.defineEntity(AbaseTestset.class, screen, "<new>");
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteAbaseTestset(Screen screen, AbaseTestset abaseTestset)
  {
    screen.getAbaseTestsets().remove(abaseTestset);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addKeyword(Screen screen, String keyword)
  {
    if (! screen.addKeyword(keyword)) {
      showMessage("screens.duplicateKeyword", "newKeyword", keyword);
    }
    else {
      _screenViewer.setNewKeyword("");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteKeyword(Screen screen, String keyword)
  {
    screen.getKeywords().remove(keyword);
    return VIEW_SCREEN;
  }

  public String findScreen(Integer screenNumber)
  {
    if (screenNumber != null) {
      Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
      if (screen != null) {
        return viewScreen(screen, null);
      }
    }
    // TODO: add message to indicate failure
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  

  // private methods
  
  private ScreenResult inflateScreenResult(int entityId)
  {
    ScreenResult screenResult = _dao.findEntityById(ScreenResult.class, entityId);
    if (screenResult != null) {
      Hibernate.initialize(screenResult.getPlateNumbers());
      Hibernate.initialize(screenResult.getResultValueTypes());
      Hibernate.initialize(screenResult.getWells());
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        rvt.getDerivedTypes();
        //rvt.getResultValues(); // major performance hit!  screenResultViewer expressly designed to not use this
        rvt.getTypesDerivedFrom();
      }
    }
    return screenResult;
  }
  
  private void initializeScreenResultBackingBeans(Screen screen, ScreenSearchResults screenSearchResults)
  {
    _lastScreen = screen;
    _lastScreenSearchResults = screenSearchResults;
    
    // TODO: HACK: makes screenResult access data-access-permissions aware 
    ScreenResult screenResult = null;
    if (screen.getScreenResult() != null) {
      screenResult = inflateScreenResult(screen.getScreenResult().getEntityId());
    }

    _screenResultImporter.setDao(_dao);
    _screenResultImporter.setMessages(getMessages());
    _screenResultImporter.setScreen(screen);
    _screenResultImporter.setScreenResultParser(new ScreenResultParser(_dao));

    _screenResultViewer.setScreen(screen);
    _screenResultViewer.setScreenResult(screenResult);
    _screenResultViewer.setDao(_dao);
    _screenResultViewer.setMessages(getMessages());
    _screenResultViewer.setScreenResultExporter(_screenResultExporter);
    _screenResultViewer.setLibrariesController(_librariesController);
    _screenResultViewer.setScreenSearchResults(screenSearchResults);

    _heatMapViewer.setDao(_dao);
    _heatMapViewer.setScreenResult(screenResult);
    _heatMapViewer.setLibrariesController(_librariesController);
  }
}

