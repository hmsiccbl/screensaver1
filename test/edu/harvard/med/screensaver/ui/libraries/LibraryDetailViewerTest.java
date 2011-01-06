// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class LibraryDetailViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(LibraryDetailViewerTest.class);

  protected LibraryDetailViewer libraryDetailViewer;

  public void testSolventType()
  {
    Library library = new Library(_admin);
    library.setScreenType(ScreenType.RNAI);
    libraryDetailViewer.editNewEntity(library);
    library.setLibraryName("y");
    library.setShortName("y");
    library.setStartPlate(2);
    library.setEndPlate(2);
    library.setLibraryType(LibraryType.COMMERCIAL);
    library.setSolvent(Solvent.RNAI_BUFFER);
    library.setScreeningStatus(LibraryScreeningStatus.ALLOWED);
    libraryDetailViewer.save();
    library = genericEntityDao.reloadEntity(library);
    assertEquals(Solvent.RNAI_BUFFER, library.getSolvent());

    libraryDetailViewer.viewEntity(library);
    libraryDetailViewer.edit();
    libraryDetailViewer.getEntity().setSolvent(Solvent.DMSO);
    assertEquals(ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT, libraryDetailViewer.save());
    // TODO: assert validation message
    //assertTrue(libraryDetailViewer.isEditMode());
    assertEquals(Solvent.RNAI_BUFFER, library.getSolvent());
  }
}
