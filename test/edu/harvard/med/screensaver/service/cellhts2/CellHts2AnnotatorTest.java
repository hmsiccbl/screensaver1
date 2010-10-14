// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cellhts2;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.analysis.cellhts2.CellHTS2;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesNegControls;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesScale;
import edu.harvard.med.screensaver.analysis.cellhts2.RMethod;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

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
                                            "screenResult.dataColumns")
                                            .getScreenResult();

    assertEquals("pre-cellHTS raw DataColumn count", 8, screenResult.getDataColumns().size());
    cellHts2Annotator.runCellhts2( RMethod.NORMALIZE_PLATES,
        screenResult,
       "testAnalysis",
       NormalizePlatesMethod.MEDIAN,
       NormalizePlatesNegControls.NEG,
       NormalizePlatesScale.ADDITIVE,
       null,
       null,
       true,
       ".",
       null);
    
    // load again, to ensure we're testing the persisted version of the data
    screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            1,
                                            true,
                                            "screenResult.dataColumns")
                                            .getScreenResult();
    assertEquals("post-cellHTS raw DataColumn count", 10, screenResult.getDataColumns().size());
    for (int i = 8; i < 10; ++i ) {
      assertTrue("DataColumn " + i + " + is cellHTS-generated",
                 screenResult.getDataColumnsList().get(i).getName().contains(CellHTS2.CELLHTS2_DATA_COLUMN_PREFIX));
    }
  }
}
