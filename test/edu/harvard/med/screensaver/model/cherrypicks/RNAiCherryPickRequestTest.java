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

import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.RNAiCherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;

import org.apache.log4j.Logger;

public class RNAiCherryPickRequestTest extends CherryPickRequestTest<RNAiCherryPickRequest>
{
  private static Logger log = Logger.getLogger(RNAiCherryPickRequestTest.class);

  protected LibrariesDAO librariesDao;
  protected RNAiCherryPickRequestAllowancePolicy rnaiCherryPickRequestAllowancePolicy;

  public RNAiCherryPickRequestTest() throws IntrospectionException
  {
    super(RNAiCherryPickRequest.class);
  }

  public void testRequestedEmptyWellsOnAssayPlate()
  {
    schemaUtil.truncateTablesOrCreateSchema();

    final Set<WellName> requestedEmptyWells= new HashSet<WellName>(Arrays.asList(new WellName("A03"),
                                                                                 new WellName("G07"),
                                                                                 new WellName("E11")));
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        CherryPickRequest cherryPickRequest = screen.createCherryPickRequest();
        cherryPickRequest.addEmptyWellsOnAssayPlate(requestedEmptyWells);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen2 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertTrue(screen2.getCherryPickRequests().iterator().next().getEmptyWellsOnAssayPlate().size() == 144 + (3 - 1 /* -1 to account for overlap w/edge wells */));
        assertTrue(screen2.getCherryPickRequests().iterator().next().getEmptyWellsOnAssayPlate().containsAll(requestedEmptyWells));
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
        Library duplexLibrary = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, PlateSize.WELLS_384);
        genericEntityDao.saveOrUpdateEntity(duplexLibrary);

        for (int plateOrdinal = 0; plateOrdinal < 6; ++plateOrdinal) {
          for (int iRow = 0; iRow < duplexLibrary.getPlateSize().getRows(); ++iRow) {
            for (int iCol = 0; iCol < duplexLibrary.getPlateSize().getColumns(); ++iCol) {
              WellKey wellKey = new WellKey(plateOrdinal + 50001, iRow, iCol);
              Well well = librariesDao.findWell(wellKey);
              cherryPickRequest.createScreenerCherryPick(well);
            }
          }
        }
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
        genericEntityDao.flush();

        assertEquals("cherry pick allowance used", 384 * 6, rnaiCherryPickRequestAllowancePolicy.getCherryPickAllowanceUsed(cherryPickRequest));
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
          genericEntityDao.saveOrUpdateEntity(CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, PlateSize.WELLS_384));
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

