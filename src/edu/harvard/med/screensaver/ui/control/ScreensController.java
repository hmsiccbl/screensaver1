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
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;

import org.apache.myfaces.custom.fileupload.UploadedFile;


/**
 * @motivation Allows Spring to create an AOP proxy for our
 *             ScreensControllerImpl concrete class, which can then be injected
 *             into other beans expecting a ScreensController type. If
 *             ScreensController was the concrete class, its proxy would not be
 *             injectable into other beans.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface ScreensController
{

  @UIControllerMethod
  public String browseScreens();

  @UIControllerMethod
  public String browseMyScreens();

  @UIControllerMethod
  public String viewScreen(final Screen screenIn,
                           SearchResults<Screen> searchResults);

  @UIControllerMethod
  public String editScreen(final Screen screen);

  @UIControllerMethod
  public String saveScreen(final Screen screen, final DAOTransaction updater);

  @UIControllerMethod
  public String deleteScreenResult(ScreenResult screenResult);

  @UIControllerMethod
  public String viewLastScreen();

  @UIControllerMethod
  public String viewScreenResultImportErrors();

  @UIControllerMethod
  public String addStatusItem(Screen screen, StatusValue statusValue);

  @UIControllerMethod
  public String deleteStatusItem(Screen screen, StatusItem statusItem);

  @UIControllerMethod
  public String addPublication(Screen screen);

  @UIControllerMethod
  public String deletePublication(Screen screen, Publication publication);

  @UIControllerMethod
  public String addLetterOfSupport(Screen screen);

  @UIControllerMethod
  public String deleteLetterOfSupport(Screen screen,
                                      LetterOfSupport letterOfSupport);

  @UIControllerMethod
  public String addAttachedFile(Screen screen);

  @UIControllerMethod
  public String deleteAttachedFile(Screen screen, AttachedFile attachedFile);

  @UIControllerMethod
  public String addFundingSupport(Screen screen, FundingSupport fundingSupport);

  @UIControllerMethod
  public String deleteFundingSupport(Screen screen,
                                     FundingSupport fundingSupport);

  @UIControllerMethod
  public String addKeyword(Screen screen, String keyword);

  @UIControllerMethod
  public String deleteKeyword(Screen screen, String keyword);

  @UIControllerMethod
  public String findScreen(Integer screenNumber);

  @UIControllerMethod
  public String findCherryPickRequest(Integer cherryPickRequestNumber);

  @UIControllerMethod
  public String importScreenResult(Screen screen,
                                   UploadedFile uploadedFile,
                                   ScreenResultParser parser);

  @UIControllerMethod
  public String downloadScreenResult(ScreenResult screenResult);

  @UIControllerMethod
  public String downloadCherryPickRequestPlateMappingFiles(CherryPickRequest cherryPickRequest,
                                                           Set<CherryPickAssayPlate> plateNames);

  @UIControllerMethod
  public String createCherryPickRequest(Screen screen);

  @UIControllerMethod
  public String editCherryPickRequest(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String saveCherryPickRequest(CherryPickRequest cherryPickRequest, DAOTransaction transaction);

  @UIControllerMethod
  public String viewCherryPickRequest(CherryPickRequest cherryPickRequestIn);

  @UIControllerMethod
  public String addCherryPicksForWells(CherryPickRequest cherryPickRequest,
                                       Set<WellKey> labCherryPickWells);

  @UIControllerMethod
  public String addCherryPicksForPoolWells(CherryPickRequest cherryPickRequest,
                                           Set<WellKey> labCherryPickWells);

  @UIControllerMethod
  public String deleteAllScreenerCherryPicks(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String viewCherryPickRequestWellVolumes(CherryPickRequest cherryPickRequest,
                                                 boolean forUnfufilledLabCherryPicksOnly);

  @UIControllerMethod
  public String allocateCherryPicks(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String deallocateCherryPicks(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String plateMapCherryPicks(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String deleteCherryPickRequest(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String recordLiquidTransferForAssayPlates(CherryPickRequest cherryPickRequest, 
                                                   Set<CherryPickAssayPlate> selectedAssayPlates,
                                                   ScreensaverUser performedBy,
                                                   Date dateOfLiquidTransfer,
                                                   String comments);
                                  
  @UIControllerMethod
  public String cancelAndDeallocateCherryPicksByPlate(CherryPickRequest cherryPickRequest,
                                                      Set<CherryPickAssayPlate> assayPlates,
                                                      ScreensaverUser performedBy,
                                                      Date dateOfLiquidTransfer,
                                                      String comments);

  @UIControllerMethod
  public String createNewCherryPickRequestForUnfulfilledCherryPicks(CherryPickRequest cherryPickRequest);

  @UIControllerMethod
  public String recordFailureOfAssayPlates(CherryPickRequest cherryPickRequest, 
                                           Set<CherryPickAssayPlate> selectedAssayPlates, 
                                           ScreensaverUser performedBy, 
                                           Date dateOfLiquidTransfer, 
                                           String comments);

  @UIControllerMethod
  public String downloadCherryPickRequest(CherryPickRequest cherryPickRequest);
}
