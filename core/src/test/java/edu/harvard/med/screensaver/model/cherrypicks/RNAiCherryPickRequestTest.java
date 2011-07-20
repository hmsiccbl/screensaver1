// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestSuite;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.RNAiCherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class RNAiCherryPickRequestTest extends CherryPickRequestTest<RNAiCherryPickRequest>
{
  public static TestSuite suite()
  {
    return buildTestSuite(RNAiCherryPickRequestTest.class, RNAiCherryPickRequest.class);
  }

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected RNAiCherryPickRequestAllowancePolicy rnaiCherryPickRequestAllowancePolicy;

  public RNAiCherryPickRequestTest()
  {
    super(RNAiCherryPickRequest.class);
  }

  public void testRequestedEmptyWellsOnAssayPlate()
  {
    schemaUtil.truncateTables();

    final Set<WellName> requestedEmptyWells= new HashSet<WellName>(Arrays.asList(new WellName("A03"),
                                                                                 new WellName("G07"),
                                                                                 new WellName("E11")));
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        CherryPickRequest cherryPickRequest = screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
        cherryPickRequest.addEmptyWellsOnAssayPlate(cherryPickRequest.getAssayPlateType().getPlateSize().getEdgeWellNames(2));
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
        Screen screen2 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
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
    schemaUtil.truncateTables();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
        Library duplexLibrary = MakeDummyEntities.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, PlateSize.WELLS_384);
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

  public void testUniqueScreenerCherryPicksOnly()
  {
    schemaUtil.truncateTables();
    try {
      Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
      RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
      Library library = MakeDummyEntities.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, PlateSize.WELLS_384);
      Well well = library.getWells().iterator().next();
      cherryPickRequest.createScreenerCherryPick(well);
      cherryPickRequest.createScreenerCherryPick(well);
      fail("DuplicateEntityException was not thrown for duplicate screener cherry picks");
    }
    catch (DuplicateEntityException e) {
      return;
    }
  }
}

