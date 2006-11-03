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

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.screens.ScreensBrowser;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;

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

  
  // private instance fields
  
  private DAO _dao;
  private ScreensBrowser _screensBrowser;
  private ScreenViewer _screenViewer;
  

  // public getters and setters
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
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
  
  public ScreenViewer getScreenViewer()
  {
    return _screenViewer;
  }
  
  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
    _screenViewer.setScreensController(this);
  }
  
  
  // public controller methods

  @UIControllerMethod
  public String browseScreens()
  {
    if (_screensBrowser.getScreenSearchResults() == null) {
      List<Screen> screens = _dao.findAllEntitiesWithType(Screen.class);
      _screensBrowser.setScreenSearchResults(new ScreenSearchResults(screens, this));
    }
    return BROWSE_SCREENS;
  }
  
  @UIControllerMethod
  public String viewScreen(Screen screen, ScreenSearchResults screenSearchResults)
  {
    _screenViewer.setScreen(screen);
    _screenViewer.setCandidateLabHeads(_dao.findAllLabHeads());
    _screenViewer.setCandidateCollaborators(_dao.findAllEntitiesWithType(ScreeningRoomUser.class));
    _screenViewer.setScreenSearchResults(screenSearchResults);
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String saveScreen(Screen screen)
  {
    try {
      _dao.persistEntity(screen);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (Throwable e) {
      reportSystemError(e);
    }
    return VIEW_SCREEN;
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
  public String addAssayReadoutType(Screen screen, AssayReadoutType assayReadoutType)
  {
    if (assayReadoutType != null) {
      screen.addAssayReadoutType(assayReadoutType);
      _screenViewer.setNewAssayReadoutType(null);
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteAssayReadoutType(Screen screen, AssayReadoutType assayReadoutType)
  {
    screen.getAssayReadoutTypes().remove(assayReadoutType);
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
}

