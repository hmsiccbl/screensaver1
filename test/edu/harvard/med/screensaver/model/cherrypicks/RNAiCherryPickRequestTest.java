// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;

import org.apache.log4j.Logger;

public class RNAiCherryPickRequestTest extends AbstractEntityInstanceTest<RNAiCherryPickRequest>
{
  private static Logger log = Logger.getLogger(RNAiCherryPickRequestTest.class);

  protected LibrariesDAO librariesDao;

  public RNAiCherryPickRequestTest() throws IntrospectionException
  {
    super(RNAiCherryPickRequest.class);
  }

  public void testRequestedEmptyColumnsOnAssayPlate()
  {
    schemaUtil.truncateTablesOrCreateSchema();

    final Set<Integer> requestedEmptyColumns = new HashSet<Integer>(Arrays.asList(3, 7, 11));
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        CherryPickRequest cherryPickRequest = screen.createCherryPickRequest();
        cherryPickRequest.addRequestedEmptyColumnsOnAssayPlate(requestedEmptyColumns);
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest); // why do we need this, if we're also persisting the screen?!
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen2 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertEquals(requestedEmptyColumns,
                     screen2.getCherryPickRequests().iterator().next().getRequestedEmptyColumnsOnAssayPlate());
      }
    });
  }

  /**
   * Note that we're test w/o creating assay plates for the lab cherry picks.  This is intentional.
   */
  public void testCherryPickAllowance()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
        genericEntityDao.saveOrUpdateEntity(CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, 384));

        for (int plateOrdinal = 0; plateOrdinal < 6; ++plateOrdinal) {
          for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
            for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
              WellKey wellKey = new WellKey(plateOrdinal + 50001, iRow, iCol);
              Well well = librariesDao.findWell(wellKey);
              cherryPickRequest.createScreenerCherryPick(well);
            }
          }
        }
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest); // avoid hib errors on flush
        genericEntityDao.saveOrUpdateEntity(screen); // avoid hib errors on flush

        assertEquals("cherry pick allowance used", 384 * 6, cherryPickRequest.getCherryPickAllowanceUsed());
      }
    });
  }

  /**
   * Test creating two screener cherry picks for the same cherry pick request / well causes
   * some kind of error. Right now, it causes a DataIntegrityViolationException on commit, so
   * we test for that.
   */
  public void testDuplicateScreenerCherryPick()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
          RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
          genericEntityDao.saveOrUpdateEntity(CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, 384));
          WellKey wellKey = new WellKey(50001, 0, 0);
          Well well = librariesDao.findWell(wellKey);
          cherryPickRequest.createScreenerCherryPick(well);
          cherryPickRequest.createScreenerCherryPick(well);
          genericEntityDao.saveOrUpdateEntity(cherryPickRequest);
        }
      });
      fail("DuplicateEntityException was not thrown for duplicate screener cherry picks");
    }
    catch (DuplicateEntityException e) {
      return;
    }
  }
}

