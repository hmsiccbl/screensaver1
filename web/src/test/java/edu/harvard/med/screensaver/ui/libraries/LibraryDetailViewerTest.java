// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class LibraryDetailViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(LibraryDetailViewerTest.class);

  @Autowired
  protected LibraryDetailViewer libraryDetailViewer;

  private Library _library;

  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    _library = new Library(_admin);
    _library.setScreenType(ScreenType.RNAI);
    libraryDetailViewer.editNewEntity(_library);
    _library.setLibraryName("y");
    _library.setShortName("y");
    _library.setStartPlate(2);
    _library.setEndPlate(2);
    _library.setLibraryType(LibraryType.COMMERCIAL);
    _library.setSolvent(Solvent.RNAI_BUFFER);
    _library.setScreeningStatus(LibraryScreeningStatus.ALLOWED);
    libraryDetailViewer.save();
    _library = libraryDetailViewer.getEntity();
  }

  public void testSolventType()
  {
    assertEquals(Solvent.RNAI_BUFFER, _library.getSolvent());

    libraryDetailViewer.viewEntity(_library);
    libraryDetailViewer.edit();
    libraryDetailViewer.getEntity().setSolvent(Solvent.DMSO);
    assertEquals(ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT, libraryDetailViewer.save());
    // TODO: assert validation message
    //assertTrue(libraryDetailViewer.isEditMode());
    assertEquals(Solvent.RNAI_BUFFER, _library.getSolvent());
  }

  public void testEntityUpdateHistory()
  {
    libraryDetailViewer.viewEntity(_library);
    assertEquals(ScreensaverConstants.BROWSE_ENTITY_UPDATE_HISTORY, libraryDetailViewer.viewUpdateHistory());
    assertEquals(1, libraryDetailViewer.getEntityUpdateSearchResults().getRowCount());
    assertEquals(Solvent.RNAI_BUFFER, _library.getSolvent());
  }

  public void testComments()
  {
    _library = genericEntityDao.reloadEntity(_library, true, Library.updateActivities.castToSubtype(Library.class));
    assertEquals(0, _library.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).size());

    libraryDetailViewer.viewEntity(_library);
    libraryDetailViewer.edit();
    libraryDetailViewer.getComments().setNewComment("new comment 1");
    libraryDetailViewer.getComments().addNewComment();
    libraryDetailViewer.save();
    assertEquals(1, libraryDetailViewer.getComments().getCommentsDataModel().getRowCount());
    assertEquals(1, libraryDetailViewer.getEntity().getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).size());
    libraryDetailViewer.viewEntity(_library);
    libraryDetailViewer.edit();
    libraryDetailViewer.getComments().setNewComment("new comment 2");
    libraryDetailViewer.getComments().addNewComment();
    libraryDetailViewer.save();
    assertEquals(2, libraryDetailViewer.getComments().getCommentsDataModel().getRowCount());
    assertEquals(2, libraryDetailViewer.getEntity().getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).size());
  }
}
