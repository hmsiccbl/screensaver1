// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.1.0-dev/test/edu/harvard/med/screensaver/db/GenericEntityDAOTest.java $
// $Id: GenericEntityDAOTest.java 4486 2010-08-04 19:52:03Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.springframework.test.annotation.IfProfileValue;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * See <a href=
 * "http://forge.abcd.harvard.edu/gf/project/screensaver/tracker/?action=TrackerItemEdit&tracker_item_id=2516"
 * >[#2516]</a>
 */
@IfProfileValue(name = "demonstrate.hibernate.bugs", value = "true")
public class ProxyInitializationTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(ProxyInitializationTest.class);

  @PersistenceContext
  protected EntityManager entityManager;

  protected void setUp() throws Exception
  {
    super.setUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
        Well well = library.getWells().iterator().next();
        Copy copy = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "A");
        copy.findPlate(1000).withPlateType(PlateType.ABGENE).withWellVolume(new Volume(10));
        genericEntityDao.persistEntity(library);

        Screen screen = dataFactory.newInstance(Screen.class);
        screen.setFacilityId("1");
        screen.setLabHead(dataFactory.newInstance(LabHead.class));
        screen.setLeadScreener(dataFactory.newInstance(LabHead.class));
        screen.setScreenType(ScreenType.RNAI);
        CherryPickRequest cpr = screen.createCherryPickRequest(dataFactory.newInstance(AdministratorUser.class),
                                                               dataFactory.newInstance(ScreeningRoomUser.class),
                                                               new LocalDate());
        cpr.setTransferVolumePerWellApproved(new Volume(1));
        cpr.setRequestedBy(screen.getLeadScreener());
        LabCherryPick lcp = cpr.createScreenerCherryPick(well).createLabCherryPick(well);
        CherryPickAssayPlate cpap = cpr.createCherryPickAssayPlate(0, 0, PlateType.ABGENE);
        lcp.setAllocated(copy);
        lcp.setMapped(cpap, 0, 0);
        AdministratorUser admin1 = new AdministratorUser("Admin1", "User");
        CherryPickLiquidTransfer cplt = screen.createCherryPickLiquidTransfer(admin1, screen.getLeadScreener(), new LocalDate(), CherryPickLiquidTransferStatus.SUCCESSFUL);
        cplt.addCherryPickAssayPlate(cpap);
        genericEntityDao.persistEntity(screen);
      }
    });
  }
  
  class TestTxn implements DAOTransaction
  {
    public CherryPickRequest _cpr;
    public CherryPickScreening _screening;

    @Override
    public void runTransaction()
    {
      log.info("initial session:" /* TODO */);
      ScreeningRoomUser originalPerformedBy = _cpr.getRequestedBy();
      log.info("original performedBy class=" + originalPerformedBy.getClass());
//      Screen screen = genericEntityDao.reloadEntity(_cpr.getScreen());
//      CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);

      /* calling Hibernate.initialize does not fix the problem */
      //Hibernate.initialize(screen.getLeadScreener()); 

      /* calling genericEntityDao.findEntityById DOES fix the problem */
      //ScreeningRoomUser reloadedPerformedBy = genericEntityDao.findEntityById(ScreeningRoomUser.class, originalPerformedBy.getEntityId());

      /* this custom HQL fixes the problem */
      //ScreeningRoomUser reloadedPerformedBy = genericEntityDao.findEntitiesByHql(ScreeningRoomUser.class, "from ScreeningRoomUser x where x = ?", originalPerformedBy).get(0);

      /* this custom HQL, which is the same as that generated by the original reloadEntity() call, recreates the problem */
      //ScreeningRoomUser reloadedPerformedBy = genericEntityDao.findEntitiesByHql(ScreeningRoomUser.class, "from LabHead x where x.id = ?", originalPerformedBy.getEntityId()).get(0);

      /* this call which requests a ScreeningRoomUser type explicitly DOES fix the problem */
      //ScreeningRoomUser reloadedPerformedBy = genericEntityDao.findEntityById(ScreeningRoomUser.class, originalPerformedBy.getEntityId(), true);

      /* this call which requests a LabHead type explicitly does NOT fix the problem */
      //ScreeningRoomUser reloadedPerformedBy = genericEntityDao.findEntityById(LabHead.class, originalPerformedBy.getEntityId(), true);

      /*
       * the original code, that causes the problem; returns a non-proxy instance, and so differs from
       * Screen.leadScreener
       */
      ScreeningRoomUser reloadedPerformedBy = genericEntityDao.reloadEntity(originalPerformedBy);

      /*
       * if we load the performedBy entity before the screen and cherryPickRequest, then these latter two can be loaded
       * with the non-proxy instance, which fixes the problem
       */
      Screen screen = genericEntityDao.reloadEntity(_cpr.getScreen());
      CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);

      assertSame(screen.getLeadScreener(), reloadedPerformedBy);
      assertSame(cpr.getRequestedBy(), reloadedPerformedBy);

      log.info("pre-eager-fetch cpr requestedBy initialized: " + Hibernate.isInitialized(cpr.getRequestedBy()));
      log.info("pre-eager-fetch screen lead screener initialized: " + Hibernate.isInitialized(screen.getLeadScreener()));
      log.info("pre-eager-fetch session:" /* TODO */);

      genericEntityDao.needReadOnly(screen, Screen.leadScreener);
      log.info("post-eager-fetch cpr requestedBy initialized: " + Hibernate.isInitialized(cpr.getRequestedBy()));
      log.info("post-eager-fetch screen lead screener initialized: " + Hibernate.isInitialized(screen.getLeadScreener()));
      log.info("post-eager-fetch session:" /* TODO */);
      assertTrue(Hibernate.isInitialized(screen.getLeadScreener()));
      assertTrue(Hibernate.isInitialized(cpr.getRequestedBy()));
    }
  };

  public void testInitializeExtantProxy()
  {
    TestTxn txn = new TestTxn();

    txn._cpr = genericEntityDao.findAllEntitiesOfType(CherryPickRequest.class, true, CherryPickRequest.requestedBy).get(0);
    log.info("pre-txn cpr requestedBy initialized: " + Hibernate.isInitialized(txn._cpr.getRequestedBy()));
    log.info("pre-txn cpr requestedBy: " + txn._cpr.getRequestedBy().getClass() + " " + txn._cpr.getRequestedBy().getEntityId());
    genericEntityDao.doInTransaction(txn);

    //    log.info("post txn screening.screen.leadScreener=" + txn._screening.getScreen().getLeadScreener().getClass() + " " +
    //      txn._screening.getScreen().getLeadScreener());

    //txn._cpr.getRequestedBy().getLastName();
//    txn._screen.getLeadScreener().getScreensaverUserId();
    //txn._screen.getLeadScreener().getEntityId();
  }
}
