// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cellhts2;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.analysis.cellhts2.CellHTS2;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.RMethod;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class CellHts2AnnotatorTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(CellHts2AnnotatorTest.class);

  protected CellHts2Annotator cellHts2Annotator;

  /**
   * Note: requires Rserve daemon to be running and listening at localhost:6311,
   * with cellHTS2 and cellHTS2Db packages available
   */
  // TODO: use a mock CellHts2Annotator
  public void testNormalizePlates()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        // note: by testing with library id=2, we test the 'plate number to sequence number' mapping code
        Library library = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 2);
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        MakeDummyEntities.makeDummyScreenResult(screen, library);
        genericEntityDao.persistEntity(library);
        genericEntityDao.persistEntity(screen);
      }
    });

    ScreenResult screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            1,
                                            true,
                                            "screenResult.resultValueTypes")
                                            .getScreenResult();

    assertEquals("pre-cellHTS raw RVT count", 8, screenResult.getResultValueTypes().size());
    cellHts2Annotator.runCellhts2( RMethod.NORMALIZE_PLATES,
        screenResult,
       "testAnalysis",
       NormalizePlatesMethod.MEDIAN,
       null,
       null,
       true,
       "");

    // load again, to ensure we're testing the persisted version of the data
    screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            1,
                                            true,
                                            "screenResult.resultValueTypes")
                                            .getScreenResult();
    assertEquals("post-cellHTS raw RVT count", 10, screenResult.getResultValueTypes().size());
    for (int i = 8; i < 10; ++i ) {
      assertTrue("RVT " + i + " + is cellHTS-generated",
                 screenResult.getResultValueTypesList().get(i).getName().contains(CellHTS2.CELLHTS2_DATA_HEADER_PREFIX));
    }
  }
}
