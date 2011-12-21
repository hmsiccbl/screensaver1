// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.Lab;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

/**
 * Tests EntityViewPolicy implementation for ICCB-L facility, as well as Hibernate interceptor-based
 * mechanism for setting "restricted" flag on entities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@ContextConfiguration(locations = { "/spring-context-test-security.xml" }, inheritLocations = false)
@Transactional
public class IccblEntityViewPolicyTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(IccblEntityViewPolicyTest.class);

  @Autowired
  protected ScreenResultsDAO screenResultsDao;
  @Autowired
  protected LibraryCreator libraryCreator;

  private IccblEntityViewPolicy entityViewPolicy;

  private LabHead me;
  private LabHead affiliate;
  private ScreeningRoomUser stranger;

  private Screen myLevel0Screen;
  private Screen myLevel1Screen;
  private Screen myLevel2Screen;
  private Screen myLevel3Screen;
  
  // screens with "overlapping hits" have exact same set of hits as "my" screens
  // screens with "non overlapping hits" have hits, but none in common with the set of hits of "my" screens
  private Screen othersLevel0ScreenWithNonOverlappingHits;
  private Screen othersLevel1ScreenWithNonOverlappingHits;
  private Screen othersLevel1ScreenWithOverlappingHits;
  private Screen othersLevel2ScreenWithNonOverlappingHits;
  private Screen othersLevel2ScreenWithOverlappingHits;
  private Screen othersLevel3ScreenWithNonOverlappingHits;
  private Screen othersLevel3ScreenWithOverlappingHits;

  private void initializeDataForDataSharingLevelTests(ScreenType screenType)
  {
    initializeMe();

    affiliate = new LabHead("Lab", "Affiliate", null);
    stranger = new ScreeningRoomUser("Strange", "Screener");
    genericEntityDao.persistEntity(affiliate);
    genericEntityDao.persistEntity(stranger);
    
    Library libForMutualPositives = MakeDummyEntities.makeDummyLibrary(1, screenType, 1);
    genericEntityDao.persistEntity(libForMutualPositives);
    Library libForNonMutualPositives = MakeDummyEntities.makeDummyLibrary(2, screenType, 1);
    genericEntityDao.persistEntity(libForNonMutualPositives);
    
    // note: "my" screens will be associated with the "me" user in individual test methods
    
    myLevel0Screen = MakeDummyEntities.makeDummyScreen(0, screenType);
    MakeDummyEntities.makeDummyScreenResult(myLevel0Screen, libForMutualPositives);
    myLevel0Screen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);

    myLevel1Screen = MakeDummyEntities.makeDummyScreen(1, screenType);
    MakeDummyEntities.makeDummyScreenResult(myLevel1Screen, libForMutualPositives);
    myLevel1Screen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);

    myLevel2Screen = MakeDummyEntities.makeDummyScreen(2, screenType);
    MakeDummyEntities.makeDummyScreenResult(myLevel2Screen, libForMutualPositives);
    myLevel2Screen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    myLevel3Screen = MakeDummyEntities.makeDummyScreen(3, screenType);
    MakeDummyEntities.makeDummyScreenResult(myLevel3Screen, libForMutualPositives);
    myLevel3Screen.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    
    genericEntityDao.persistEntity(myLevel0Screen);
    genericEntityDao.persistEntity(myLevel1Screen);
    genericEntityDao.persistEntity(myLevel2Screen);
    genericEntityDao.persistEntity(myLevel3Screen);

    othersLevel0ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(100, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel0ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel0ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    genericEntityDao.persistEntity(othersLevel0ScreenWithNonOverlappingHits);

    othersLevel1ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(110, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel1ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel1ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    genericEntityDao.persistEntity(othersLevel1ScreenWithNonOverlappingHits);

    othersLevel1ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(111, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel1ScreenWithOverlappingHits, libForMutualPositives);
    othersLevel1ScreenWithOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    genericEntityDao.persistEntity(othersLevel1ScreenWithOverlappingHits);

    othersLevel2ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(120, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel2ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel2ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    genericEntityDao.persistEntity(othersLevel2ScreenWithNonOverlappingHits);

    othersLevel2ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(121, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel2ScreenWithOverlappingHits, libForMutualPositives);
    othersLevel2ScreenWithOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    genericEntityDao.persistEntity(othersLevel2ScreenWithOverlappingHits);

    othersLevel3ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(130, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel3ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel3ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    genericEntityDao.persistEntity(othersLevel3ScreenWithNonOverlappingHits);

    othersLevel3ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(131, screenType);
    MakeDummyEntities.makeDummyScreenResult(othersLevel3ScreenWithOverlappingHits, libForMutualPositives);
    othersLevel3ScreenWithOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    genericEntityDao.persistEntity(othersLevel3ScreenWithOverlappingHits);

    genericEntityDao.flush();
  }

  private void initializeMe()
  {
    me = new LabHead("Test", "Screener", null);
    genericEntityDao.persistEntity(me);
    setCurrentUser(me);
  }
  
  public void testMutualUserViewingPermissions()
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[4];
    users[0] = makeUserWithRoles(true);
    users[1] = makeUserWithRoles(false);
    users[2] = makeUserWithRoles(false);
    users[3] = makeUserWithRoles(false);
    users[1].setLab(users[0].getLab());
    users[2].setLab(users[0].getLab());
    genericEntityDao.flush();
   
    {
      List<ScreeningRoomUser> filteredUsers = genericEntityDao.findAllEntitiesOfType(ScreeningRoomUser.class);
      setCurrentUser(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                     users[1].getScreensaverUserId()));
      assertTrue(filteredUsers.contains(entityViewPolicy.getScreensaverUser()));
      for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
        ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
        if (entityViewPolicy.visit(user) == null) {
          iter.remove();
        }
      }
      assertTrue("user can view own account ", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[1].getScreensaverUserId())));
      assertTrue("user can view account of user in same lab", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[2].getScreensaverUserId())));
      assertTrue("user can view account of lab head", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[0].getScreensaverUserId())));
      assertFalse("user cannot view account of user not in same lab", 
                  filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                         users[3].getScreensaverUserId())));
    }

    {
      List<ScreeningRoomUser> filteredUsers = genericEntityDao.findAllEntitiesOfType(ScreeningRoomUser.class);
      setCurrentUser(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                                users[0].getScreensaverUserId()));
      for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
        ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
        if (entityViewPolicy.visit(user) == null) {
          iter.remove();
        }
      }
      assertTrue("lab head can view own account", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[0].getScreensaverUserId())));
      assertTrue("lab head can view account of user in same lab", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[1].getScreensaverUserId())));
      assertTrue("lab head can view account of user in same lab", 
                 filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                        users[2].getScreensaverUserId())));
      assertFalse("lab head cannot view account of user not in same lab",
                  filteredUsers.contains(genericEntityDao.findEntityById(ScreeningRoomUser.class,
                                                                         users[3].getScreensaverUserId())));
    }
  }

  /**
   * Tests basic entity viewing permissions of screens:
   * <ul>
   * <li>for Small Molecule and RNAi screens</li>
   * <li>for public and private DSLs (level 0 and 3, not levels 1 and 2, which are more thoroughly tested elsewhere)</li>
   * <li>for users with DSL 3 roles that are invalid and valid for screen's screen type</li>
   * <li>screens' child entities (screen result, lab activities, cprs)</li>
   * <ul>
   * There is overlap between this test and the more substantial tests for screens of level 1 and 3, but no harm in that
   */
  public void testScreenPermissions()
  {
    ScreeningRoomUser rnaiScreener = makeUserWithRoles(false, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    ScreeningRoomUser smallMoleculeScreener = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    ScreeningRoomUser smallMoleculeRnaiScreener = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);

    Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    rnaiScreen.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    rnaiScreen.createScreenResult();
    rnaiScreen.createCherryPickRequest((AdministratorUser) rnaiScreen.getCreatedBy());
    rnaiScreen.createLibraryScreening((AdministratorUser) rnaiScreen.getCreatedBy(), rnaiScreener, new LocalDate());

    Screen publicRnaiScreen = MakeDummyEntities.makeDummyScreen(10, ScreenType.RNAI);
    publicRnaiScreen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publicRnaiScreen.createScreenResult();
    
    Screen smallMoleculeScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    smallMoleculeScreen.createScreenResult();
    smallMoleculeScreen.createCherryPickRequest((AdministratorUser) smallMoleculeScreen.getCreatedBy());
    smallMoleculeScreen.createLibraryScreening((AdministratorUser) rnaiScreen.getCreatedBy(), rnaiScreener, new LocalDate());

    Screen publicSmallMoleculeScreen = MakeDummyEntities.makeDummyScreen(20, ScreenType.SMALL_MOLECULE);
    publicSmallMoleculeScreen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publicSmallMoleculeScreen.createScreenResult();
    
    rnaiScreen.setLeadScreener(rnaiScreener);
    smallMoleculeScreen.setLeadScreener(smallMoleculeScreener);
    rnaiScreen.addCollaborator(smallMoleculeRnaiScreener);
    smallMoleculeScreen.addCollaborator(smallMoleculeRnaiScreener);
    
    genericEntityDao.saveOrUpdateEntity(rnaiScreener);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeScreener);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeRnaiScreener);
    genericEntityDao.saveOrUpdateEntity(rnaiScreen);
    genericEntityDao.saveOrUpdateEntity(publicRnaiScreen);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeScreen);
    genericEntityDao.saveOrUpdateEntity(publicSmallMoleculeScreen);
    genericEntityDao.flush();
    
    setCurrentUser(rnaiScreener);
    assertNotNull("rnai user is not restricted from own rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertNotNull("rnai user is not restricted from own rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertNotNull("rnai user is not restricted from own rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertNotNull("rnai user is not restricted from own rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertNull("rnai user is restricted from others' private small molecule screen", entityViewPolicy.visit(smallMoleculeScreen));
    assertNull("rnai user is restricted from others' private small molecule screen result", entityViewPolicy.visit(smallMoleculeScreen.getScreenResult()));
    assertNull("rnai user is restricted from others' private small molecule screen CPRs", entityViewPolicy.visit((SmallMoleculeCherryPickRequest) smallMoleculeScreen.getCherryPickRequests().iterator().next()));
    assertNull("rnai user is restricted from others' private small molecule screen lab activities", entityViewPolicy.visit((LibraryScreening) smallMoleculeScreen.getLabActivities().iterator().next())); // 
    assertNotNull("rnai user is not restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertNotNull("rnai user is not restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
    assertNull("rnai user is restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertNull("rnai user is restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    setCurrentUser(smallMoleculeScreener);
    assertNotNull("small molecule user is not restricted from own small molecule screen", entityViewPolicy.visit(smallMoleculeScreen));
    assertNotNull("small molecule user is not restricted from own small molecule screen result", entityViewPolicy.visit(smallMoleculeScreen.getScreenResult()));
    assertNotNull("small molecule user is not restricted from own small molecule screen CPRs", entityViewPolicy.visit((SmallMoleculeCherryPickRequest) smallMoleculeScreen.getCherryPickRequests().iterator().next()));
    assertNotNull("small molecule user is not restricted from own small molecule screen lab activities", entityViewPolicy.visit((LibraryScreening) smallMoleculeScreen.getLabActivities().iterator().next()));
    assertNull("small molecule user is restricted from others' private rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertNull("small molecule user is restricted from others' private rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertNull("small molecule user is restricted from others' private rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertNull("small molecule user is restricted from others' private rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertNotNull("small molecule user is not restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertNotNull("small molecule user is not restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    assertNull("small molecule user is restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertNull("small molecule user is restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
    setCurrentUser(smallMoleculeRnaiScreener);
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertNotNull("small molecule+rnai user is not restricted from own rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertNotNull("small molecule+rnai user is not restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertNotNull("small molecule+rnai user is not restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    assertNotNull("small molecule+rnai user is not restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertNotNull("small molecule+rnai user is not restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
    smallMoleculeScreen.removeCollaborator(smallMoleculeRnaiScreener);
    rnaiScreen.removeCollaborator(smallMoleculeRnaiScreener);
    genericEntityDao.flush();
    setCurrentUser(smallMoleculeRnaiScreener);
    assertNull("small molecule+rnai user is restricted from others' private small molecule screen", entityViewPolicy.visit(smallMoleculeScreen));
    assertNull("small molecule+rnai user is restricted from others' private small molecule screen result", entityViewPolicy.visit(smallMoleculeScreen.getScreenResult()));
    assertNull("small molecule+rnai user is restricted from others' private rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertNull("small molecule+rnai user is restricted from others' private rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
  }
  
  public void testGrayAdminRestrictions()
  {
    AdministratorUser dataEntryAdmin = new AdministratorUser("DataEntry", "Admin");
    AdministratorUser grayAdmin = new AdministratorUser("Gray", "Admin");
    grayAdmin.addScreensaverUserRole(ScreensaverUserRole.GRAY_ADMIN);
    AdministratorUser normalAdmin = new AdministratorUser("Normal", "Admin");
    normalAdmin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
    Screen nonGrayScreen = MakeDummyEntities.makeDummyScreen(2);
    nonGrayScreen.createCherryPickRequest((AdministratorUser) nonGrayScreen.getCreatedBy());
    nonGrayScreen.createLibraryScreening(dataEntryAdmin, nonGrayScreen.getLeadScreener(), new LocalDate());
    nonGrayScreen.createScreenResult();
    Screen grayScreen = MakeDummyEntities.makeDummyScreen(1);
    FundingSupport grayFundingSupport = new FundingSupport(IccblEntityViewPolicy.GRAY_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME);
    grayScreen.addFundingSupport(grayFundingSupport);
    grayScreen.createCherryPickRequest((AdministratorUser) grayScreen.getCreatedBy());
    grayScreen.createLibraryScreening(dataEntryAdmin, grayScreen.getLeadScreener(), new LocalDate());
    grayScreen.createScreenResult();
    genericEntityDao.persistEntity(grayFundingSupport);
    genericEntityDao.persistEntity(grayAdmin);
    genericEntityDao.persistEntity(normalAdmin);
    genericEntityDao.persistEntity(grayScreen);
    genericEntityDao.persistEntity(nonGrayScreen);
    genericEntityDao.flush();

    
    setCurrentUser(grayAdmin);
    assertNotNull(entityViewPolicy.visit(grayScreen));
    assertNotNull(entityViewPolicy.visit(grayScreen.getScreenResult()));
    assertNotNull(entityViewPolicy.visit((LibraryScreening) grayScreen.getLabActivities().iterator().next()));
    assertNotNull(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) grayScreen.getCherryPickRequests().iterator().next()));
    assertNull(entityViewPolicy.visit(nonGrayScreen));
    assertNull(entityViewPolicy.visit(nonGrayScreen.getScreenResult()));
    assertNull(entityViewPolicy.visit((LibraryScreening) nonGrayScreen.getLabActivities().iterator().next()));
    assertNull(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonGrayScreen.getCherryPickRequests().iterator().next()));

    setCurrentUser(normalAdmin);
    assertNotNull(entityViewPolicy.visit(grayScreen));
    assertNotNull(entityViewPolicy.visit(grayScreen.getScreenResult()));
    assertNotNull(entityViewPolicy.visit((LibraryScreening) grayScreen.getLabActivities().iterator().next()));
    assertNotNull(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) grayScreen.getCherryPickRequests().iterator().next()));
    assertNotNull(entityViewPolicy.visit(nonGrayScreen));
    assertNotNull(entityViewPolicy.visit(nonGrayScreen.getScreenResult()));
    assertNotNull(entityViewPolicy.visit((LibraryScreening) nonGrayScreen.getLabActivities().iterator().next()));
    assertNotNull(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonGrayScreen.getCherryPickRequests().iterator().next()));
  }
  
  public void testSilencingReagentSequenceRestriction()
  {
    AdministratorUser admin = new AdministratorUser("Admin", "User");
    Library library = new Library(admin,
                                  "rnai library",
                                  "rnai lib",
                                  ScreenType.RNAI,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    library.createContentsVersion(admin);
    SilencingReagent reagent = 
      library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createSilencingReagent(new ReagentVendorIdentifier("vendor", "sirnai1"), SilencingReagentType.SIRNA, "ACTG");
    reagent.withRestrictedSequence(true);

    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);

    ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User");
    screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    screener.addScreensaverUserRole(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);

    genericEntityDao.persistEntity(library);
    genericEntityDao.persistEntity(admin);
    genericEntityDao.persistEntity(screener);
    
    genericEntityDao.flush();

    setCurrentUser(admin);
    reagent = genericEntityDao.findAllEntitiesOfType(SilencingReagent.class).get(0);
    reagent.setEntityViewPolicy(entityViewPolicy);
    assertNotNull("admin can access SilencingReagent.sequence", ((SilencingReagent) reagent.restrict()).getSequence());

    setCurrentUser(screener);
    reagent = genericEntityDao.findAllEntitiesOfType(SilencingReagent.class).get(0);
    reagent.setEntityViewPolicy(entityViewPolicy);
    assertNull("screener cannot access SilencingReagent.sequence", ((SilencingReagent) reagent.restrict()).getSequence());
  }
  
  private ScreeningRoomUser makeUserWithRoles(boolean isLabHead, ScreensaverUserRole... roles)
  {
    
    ScreeningRoomUser user;
    if (isLabHead) {
      user = new LabHead("first", 
                         "last" + new Object().hashCode(),
                         null);
    }
    else {
      user = new ScreeningRoomUser("first",
                                   "last" + new Object().hashCode());
    }
    for (ScreensaverUserRole role : roles) {
      user.addScreensaverUserRole(role);
    }
    genericEntityDao.saveOrUpdateEntity(user);
    return user;
  }
  
  public void testSmallMoleculeUserLevel1ScreenPermissions()
  {
    doTestUserLevel1ScreenPermissions(ScreenType.SMALL_MOLECULE);
  }

  public void testRNAiUserLevel1ScreenPermissions()
  {
    doTestUserLevel1ScreenPermissions(ScreenType.RNAI);
  }

  private void doTestUserLevel1ScreenPermissions(ScreenType screenType)
  {
    initializeDataForDataSharingLevelTests(screenType);
    me.addScreensaverUserRole(DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(screenType, 1));

    makeMeHaveNoScreens();
    assertAllOthersNonPublicScreensNotVisible();
    assertOthersPublicScreensVisible();

    // all of my screens have screen result data
    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);

    // assert that a level 1 user without deposited data in level 0 or 1 screens is to be treated same as a level 3 user
    // note: only my level 0 and level 1 screens's data count as "deposited data"
    // TODO: should test in reverse order, too
    screenResultsDao.deleteScreenResult(myLevel1Screen.getScreenResult());
    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);

    screenResultsDao.deleteScreenResult(myLevel0Screen.getScreenResult());
    genericEntityDao.flush();
    entityViewPolicy.update();
    // now "I" have no more deposited data in level 0/1 screens, but still do in my level 2/3 screens
    doTestUserLevel3ScreenPermissionsPostInit(screenType);

    screenResultsDao.deleteScreenResult(myLevel3Screen.getScreenResult());
    screenResultsDao.deleteScreenResult(myLevel2Screen.getScreenResult());
    genericEntityDao.flush();
    entityViewPolicy.update();
    // now "I" have no more deposited data in any of my screens
    // (note: if I am level 1 user and only have a level 2 screen (w/data),
    // others can see my level 2 screen overlapping hits, even though I cannot
    // see theirs. This is okay, since this situation should never really
    // happen, since my level 2 screen should have been moved to level 1 when
    // my user level changed from from 2 to 1)
    doTestUserLevel3ScreenPermissionsPostInit(screenType);
  }

  public void testSmallMoleculeUserLevel2ScreenPermissions()
  {
    doTestUserLevel2ScreenPermissions(ScreenType.SMALL_MOLECULE);
  }

  public void testRNAiUserLevel2ScreenPermissions()
  {
    doTestUserLevel2ScreenPermissions(ScreenType.RNAI);
  }

  private void doTestUserLevel2ScreenPermissions(ScreenType screenType)
  {
    initializeDataForDataSharingLevelTests(screenType);
    me.addScreensaverUserRole(DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(screenType, 2));
    
    makeMeHaveNoScreens();
    assertAllOthersNonPublicScreensNotVisible();
    assertOthersPublicScreensVisible();

    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions(screenType);
  }
  
  public void testSmallMoleculeUserLevel3ScreenPermissions()
  {
    doTestUserLevel3ScreenPermissions(ScreenType.SMALL_MOLECULE);
  }

  public void testRNAiUserLevel3ScreenPermissions()
  {
    doTestUserLevel3ScreenPermissions(ScreenType.RNAI);
  }

  private void doTestUserLevel3ScreenPermissions(ScreenType screenType)
  {
    initializeDataForDataSharingLevelTests(screenType);
    me.addScreensaverUserRole(DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(screenType, 3));
    
    doTestUserLevel3ScreenPermissionsPostInit(screenType);
  }

  /**
   * @motivation allow testing of assertions for user DSL 3, when user is actually DSL 1, to handle the special case of
   *             the "user dsl 1 w/o deposited data = dsl 3"; called by doTestUserLevel1ScreenPermissions(), which will
   *             have already initialized the test data; thus the "PostInit" prefix of the method name
   */
  private void doTestUserLevel3ScreenPermissionsPostInit(ScreenType screenType)
  {
    makeMeHaveNoScreens();
    assertOthersPublicScreensVisible();
    assertAllOthersNonPublicScreensNotVisible();

    makeMeLabHeadOfMyScreens();
    assertLevel3UserScreenPermissions();
    
    makeMeLeadScreenerOfMyScreens();
    assertLevel3UserScreenPermissions();
    
    makeMeCollaboratorOfMyScreens();
    assertLevel3UserScreenPermissions();
  }

  /** Tests requirements of ICCB-L SM User Agreement sec. 1.1.b, and 1.2.b */
  private void assertLevel1Or2UserScreenPermissions(ScreenType screenType)
  {
    assertMyScreensVisible();
    assertOthersPublicScreensVisible();
    
    // level 1 screeners can see details of other level 1 screens, regardless of overlapping hits
    boolean expectedVisible = me.getScreensaverUserRoles().contains(DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(screenType, 1));

    assertNotNull("others' level 1 screen with non-overlapping hits visibility", entityViewPolicy.visit(othersLevel1ScreenWithNonOverlappingHits));
    assertEquals("others' level 1 screen with non-overlapping hits, details visibility", expectedVisible, entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel1ScreenWithNonOverlappingHits));
    assertEquals("others' level 1 screen with non-overlapping hits, screen result visibility", expectedVisible, entityViewPolicy.visit(othersLevel1ScreenWithNonOverlappingHits.getScreenResult()) != null);
    assertEquals("others' level 1 screen with non-overlapping hits, non-overlapping hits visibility", expectedVisible, entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithNonOverlappingHits)) != null);
    assertEquals("others' level 1 screen with non-overlapping hits, positives data column visibility", expectedVisible, entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithNonOverlappingHits).getDataColumn()) != null);
    assertEquals("others' level 1 screen with non-overlapping hits, non-positives data column visibility", expectedVisible, entityViewPolicy.visit(findNonPositivesDataColumn(othersLevel1ScreenWithNonOverlappingHits)) != null);

    assertNotNull("others' level 1 screen with overlapping hits visible", entityViewPolicy.visit(othersLevel1ScreenWithOverlappingHits));
    assertEquals("others' level 1 screen with overlapping hits, details visibility", expectedVisible, entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel1ScreenWithOverlappingHits));
    assertEquals("others' level 1 screen with overlapping hits, screen result visibility", expectedVisible, entityViewPolicy.visit(othersLevel1ScreenWithOverlappingHits.getScreenResult()) != null);
    assertNotNull("others' level 1 screen with overlapping hits, overlapping hits visible", entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithOverlappingHits)));
    assertNotNull("others' level 1 screen with overlapping hits, positives data column visible", entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithOverlappingHits).getDataColumn()));
    assertEquals("others' level 1 screen with overlapping hits, non-positives data column visibility", expectedVisible, entityViewPolicy.visit(findNonPositivesDataColumn(othersLevel1ScreenWithOverlappingHits)) != null);

    assertNotNull("others' level 2 screen with non-overlapping hits visible", entityViewPolicy.visit(othersLevel2ScreenWithNonOverlappingHits));
    assertFalse("others' level 2 screen with non-overlapping hits visible, but not details", entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel2ScreenWithNonOverlappingHits));
    assertNull("others' level 2 screen with non-overlapping hits, screen result not visible", entityViewPolicy.visit(othersLevel2ScreenWithNonOverlappingHits.getScreenResult()));
    assertNull("others' level 2 screen with non-overlapping hits, non-overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithNonOverlappingHits)));
    assertNull("others' level 2 screen with non-overlapping hits, positives data column not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithNonOverlappingHits).getDataColumn()));
    assertNull("others' level 2 screen with non-overlapping hits, non-positives data column not visible", entityViewPolicy.visit(findNonPositivesDataColumn(othersLevel2ScreenWithNonOverlappingHits)));

    assertNotNull("others' level 2 screen with overlapping hits visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
    assertFalse("others' level 2 screen with overlapping hits visible, but not details", entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel2ScreenWithOverlappingHits));
    assertNull("others' level 2 screen with overlapping hits, screen result not visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits.getScreenResult()));
    assertNotNull("others' level 2 screen with overlapping hits, overlapping hits visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits)));
    assertNotNull("others' level 2 screen with overlapping hits, positives data column visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits).getDataColumn()));
    assertNull("others' level 2 screen with overlapping hits, non-positives data column not visible", entityViewPolicy.visit(findNonPositivesDataColumn(othersLevel2ScreenWithOverlappingHits)));
    
    // my level 0, 1 and 2 screens count towards overlapping hit viewing, but not my level 3 screens
    // TODO: this is NOT testing what it purports to test, since othersLevel2ScreenWithOverlappingHits also overlaps my 0,1,2 screens!
//    assertFalse("others' level 2 screen with overlapping hits, overlapping only my level 3 screen, not visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
//    assertFalse("others' level 2 screen with overlapping hits, overlapping only my level 3 screen, overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits)));

    assertNull("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithNonOverlappingHits));
  }

  private ResultValue findSomePositive(Screen screen)
  {
    for (DataColumn col : Iterables.reverse(screen.getScreenResult().getDataColumnsList())) {
      if (col.isPositiveIndicator()) {
        for (ResultValue rv : col.getResultValues()) {
          if (rv.isPositive()) {
            return rv;
          }
        }
      }
    }
    throw new IllegalStateException("bad test data: expected at least one 'positive' result value in screen result");
  }

  private DataColumn findNonPositivesDataColumn(Screen screen)
  {
    DataColumn col = screen.getScreenResult().getDataColumns().first();
    assert !col.isPositiveIndicator();
    return col;
  }

  private void assertLevel3UserScreenPermissions()
  {
    assertOthersPublicScreensVisible();
    assertMyScreensVisible();
    assertAllOthersNonPublicScreensNotVisible();
  }

  /**
   * Creates 3 screens, where each screen has one positive and one non-positive
   * result value; two of the screens have a mutual positive and the third
   * screen has a non-mutual positive with the first two.
   */
  private List<Screen> initScreensForMutualPositivesTests()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    Iterator<Well> wellsIter = library.getWells().iterator();
    Well well1 = wellsIter.next();
    Well well2 = wellsIter.next();
    
    Screen myScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    myScreen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    ScreenResult myScreenResult = myScreen.createScreenResult();
    DataColumn myCol = myScreenResult.createDataColumn("myCol").forReplicate(1);
    myCol.makeBooleanPositiveIndicator();
    AssayWell myAssayWell1 = myScreenResult.createAssayWell(well1);
    ResultValue myResultValue1 = myCol.createBooleanPositiveResultValue(myAssayWell1, true, false);
    assert myResultValue1.isPositive();
    AssayWell myAssayWell2 = myScreenResult.createAssayWell(well2);
    ResultValue myResultValue2 = myCol.createBooleanPositiveResultValue(myAssayWell2, false, false);
    assert !myResultValue2.isPositive();
    
    Screen othersNonMutualPositivesScreen = MakeDummyEntities.makeDummyScreen(3, ScreenType.SMALL_MOLECULE);
    othersNonMutualPositivesScreen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    ScreenResult othersNonMutualPositivesScreenResult = othersNonMutualPositivesScreen.createScreenResult();
    DataColumn othersNonMutualPositivesCol = othersNonMutualPositivesScreenResult.createDataColumn("othersNonMutualPositivesCol").forReplicate(1);
    othersNonMutualPositivesCol.makeBooleanPositiveIndicator();
    AssayWell othersNonMutualPositivesAssayWell1 = othersNonMutualPositivesScreenResult.createAssayWell(well1);
    ResultValue othersNonMutualPositivesResultValue1 = othersNonMutualPositivesCol.createBooleanPositiveResultValue(othersNonMutualPositivesAssayWell1, false, false);
    assert !othersNonMutualPositivesResultValue1.isPositive();
    AssayWell othersNonMutualPositivesAssayWell2 = othersNonMutualPositivesScreenResult.createAssayWell(well2);
    ResultValue othersNonMutualPositivesResultValue2 = othersNonMutualPositivesCol.createBooleanPositiveResultValue(othersNonMutualPositivesAssayWell2, true, false);
    assert othersNonMutualPositivesResultValue2.isPositive();

    // create another screen that *does* have mutual positives, as a sanity check
    Screen othersMutualPositivesScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    othersMutualPositivesScreen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    ScreenResult othersMutualPositivesScreenResult = othersMutualPositivesScreen.createScreenResult();
    DataColumn othersMutualPositivesCol = othersMutualPositivesScreenResult.createDataColumn("othersMutualPositivesCol").forReplicate(1);
    othersMutualPositivesCol.makeBooleanPositiveIndicator();
    AssayWell othersMutualPositivesAssayWell1 = othersMutualPositivesScreenResult.createAssayWell(well1);
    ResultValue othersMutualPositivesResultValue1 = othersMutualPositivesCol.createBooleanPositiveResultValue(othersMutualPositivesAssayWell1, true, false);
    assert othersMutualPositivesResultValue1.isPositive();
    AssayWell othersMutualPositivesAssayWell2 = othersMutualPositivesScreenResult.createAssayWell(well2);
    ResultValue othersMutualPositivesResultValue2 = othersMutualPositivesCol.createBooleanPositiveResultValue(othersMutualPositivesAssayWell2, false, false);
    assert !othersMutualPositivesResultValue2.isPositive();
    
    myScreen.getLabHead().addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    othersNonMutualPositivesScreen.getLabHead().addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    othersMutualPositivesScreen.getLabHead().addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    
    genericEntityDao.persistEntity(library);
    genericEntityDao.persistEntity(myScreen);
    genericEntityDao.persistEntity(othersNonMutualPositivesScreen);
    genericEntityDao.persistEntity(othersMutualPositivesScreen);
    genericEntityDao.flush();
    
    return Lists.newArrayList(myScreen, othersNonMutualPositivesScreen, othersMutualPositivesScreen);
  }

  public void testPairOfScreensWithMutualWellsButDisjointPositivesNotConsideredMutual()
  {
    List<Screen> screens = initScreensForMutualPositivesTests();
    Screen myScreen = screens.get(0);
    Screen othersNonMutualPositivesScreen = screens.get(1);
    Screen othersMutualPositivesScreen = screens.get(2);
    setCurrentUser(myScreen.getLabHead());
    assertNotNull("others mutual positives screen is visible (sanity check)", entityViewPolicy.visit(othersMutualPositivesScreen));
    assertTrue("others non-mutual positives screen is visible, but not details", 
               entityViewPolicy.visit(othersNonMutualPositivesScreen) != null &&
                 !entityViewPolicy.isAllowedAccessToScreenDetails(othersNonMutualPositivesScreen));
    setCurrentUser(othersNonMutualPositivesScreen.getLabHead());
    assertTrue("my non-mutual positives screen details are not visible to others", 
               entityViewPolicy.visit(myScreen) != null && !entityViewPolicy.isAllowedAccessToScreenDetails(myScreen));
  }
  
  // test that positives on screen A that are not *mutual positives* with screen B are in fact restricted
  public void testOnlyMutualPositivesAreShared()
  {
    List<Screen> screens = initScreensForMutualPositivesTests();
    Screen myScreen = screens.get(0);
    Screen othersNonMutualPositivesScreen = screens.get(1);
    Screen othersMutualPositivesScreen = screens.get(2);
    setCurrentUser(myScreen.getLabHead());
    ResultValue myPositive = findSomePositive(myScreen);
    ResultValue nonMutualPositive = findSomePositive(othersNonMutualPositivesScreen);
    ResultValue mutualPositive = findSomePositive(othersMutualPositivesScreen);
    assert !!!nonMutualPositive.getWell().equals(myPositive.getWell());
    assert mutualPositive.getWell().equals(myPositive.getWell());
    assertNull("non-mutual positive result value is not visible", entityViewPolicy.visit(nonMutualPositive));
    assertNotNull("mutual positive result value is visible", entityViewPolicy.visit(mutualPositive));
  }
  
  private void makeMeHaveNoScreens()
  {
    myLevel0Screen.setLabHead(affiliate);
    myLevel1Screen.setLabHead(affiliate);
    myLevel2Screen.setLabHead(affiliate);
    myLevel3Screen.setLabHead(affiliate);
    myLevel0Screen.setLeadScreener(affiliate);
    myLevel1Screen.setLeadScreener(affiliate);
    myLevel2Screen.setLeadScreener(affiliate);
    myLevel3Screen.setLeadScreener(affiliate);
    me.removeScreenCollaborated(myLevel0Screen);
    me.removeScreenCollaborated(myLevel1Screen);
    me.removeScreenCollaborated(myLevel2Screen);
    me.removeScreenCollaborated(myLevel3Screen);
    genericEntityDao.flush();
    entityViewPolicy.update();
  }

  private void makeMeCollaboratorOfMyScreens()
  {
    myLevel0Screen.setLabHead(affiliate);
    myLevel1Screen.setLabHead(affiliate);
    myLevel2Screen.setLabHead(affiliate);
    myLevel3Screen.setLabHead(affiliate);
    myLevel0Screen.setLeadScreener(affiliate);
    myLevel1Screen.setLeadScreener(affiliate);
    myLevel2Screen.setLeadScreener(affiliate);
    myLevel3Screen.setLeadScreener(affiliate);
    me.addScreenCollaborated(myLevel0Screen);
    me.addScreenCollaborated(myLevel1Screen);
    me.addScreenCollaborated(myLevel2Screen);
    me.addScreenCollaborated(myLevel3Screen);
    genericEntityDao.flush();
    entityViewPolicy.update();
  }

  private void makeMeLeadScreenerOfMyScreens()
  {
    myLevel0Screen.setLabHead(affiliate);
    myLevel1Screen.setLabHead(affiliate);
    myLevel2Screen.setLabHead(affiliate);
    myLevel3Screen.setLabHead(affiliate);
    myLevel0Screen.setLeadScreener(me);
    myLevel1Screen.setLeadScreener(me);
    myLevel2Screen.setLeadScreener(me);
    myLevel3Screen.setLeadScreener(me);
    me.removeScreenCollaborated(myLevel0Screen);
    me.removeScreenCollaborated(myLevel1Screen);
    me.removeScreenCollaborated(myLevel2Screen);
    me.removeScreenCollaborated(myLevel3Screen);
    genericEntityDao.flush();
    entityViewPolicy.update();
  }
  
  private void makeMeLabHeadOfMyScreens()
  {
    myLevel0Screen.setLabHead(me);
    myLevel1Screen.setLabHead(me);
    myLevel2Screen.setLabHead(me);
    myLevel3Screen.setLabHead(me);
    myLevel0Screen.setLeadScreener(affiliate);
    myLevel1Screen.setLeadScreener(affiliate);
    myLevel2Screen.setLeadScreener(affiliate);
    myLevel3Screen.setLeadScreener(affiliate);
    me.removeScreenCollaborated(myLevel0Screen);
    me.removeScreenCollaborated(myLevel1Screen);
    me.removeScreenCollaborated(myLevel2Screen);
    me.removeScreenCollaborated(myLevel3Screen);
    genericEntityDao.flush();
    entityViewPolicy.update();
  }

  private void assertMyScreensVisible()
  {
    assertNotNull("my level 0 screen visible", entityViewPolicy.visit(myLevel0Screen));
    assertNotNull("my level 1 screen visible", entityViewPolicy.visit(myLevel1Screen));
    assertNotNull("my level 2 screen visible", entityViewPolicy.visit(myLevel2Screen));
    assertNotNull("my level 3 screen visible", entityViewPolicy.visit(myLevel3Screen));
  }
  
  private void assertOthersPublicScreensVisible()
  {
    assertNotNull("others' level 0 screens visible", entityViewPolicy.visit(othersLevel0ScreenWithNonOverlappingHits));
  }

  private void assertAllOthersNonPublicScreensNotVisible()
  {
    assertNull("others' level 1 screens not visible", entityViewPolicy.visit(othersLevel1ScreenWithOverlappingHits));
    assertNull("others' level 1 screens not visible", entityViewPolicy.visit(othersLevel1ScreenWithNonOverlappingHits));
    ResultValue level1ScreenPositive = findSomePositive(othersLevel1ScreenWithOverlappingHits);
    assertNull("others' level 1 screens overlapping hits not visible", entityViewPolicy.visit(level1ScreenPositive));
    assertNull("others' level 1 screens overlapping hits data column not visible", entityViewPolicy.visit(level1ScreenPositive.getDataColumn()));
    // TODO: test non-positives data column
    assertNull("others' level 1 screens overlapping hits screen result not visible", entityViewPolicy.visit(level1ScreenPositive.getDataColumn().getScreenResult()));

    assertNull("others' level 2 screens not visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
    assertNull("others' level 2 screens not visible", entityViewPolicy.visit(othersLevel2ScreenWithNonOverlappingHits));
    ResultValue level2ScreenPositive = findSomePositive(othersLevel2ScreenWithOverlappingHits);
    assertNull("others' level 2 screens overlapping hits not visible", entityViewPolicy.visit(level2ScreenPositive));
    assertNull("others' level 2 screens overlapping hits data column not visible", entityViewPolicy.visit(level2ScreenPositive.getDataColumn()));
    assertNull("others' level 2 screens overlapping hits screen result not visible", entityViewPolicy.visit(level2ScreenPositive.getDataColumn().getScreenResult()));

    assertNull("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithOverlappingHits));
    assertNull("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithNonOverlappingHits));
    ResultValue level3ScreenPositive = findSomePositive(othersLevel3ScreenWithOverlappingHits);
    assertNull("others' level 3 screens overlapping hits not visible", entityViewPolicy.visit(level3ScreenPositive));
    assertNull("others' level 3 screens overlapping hits data column not visible", entityViewPolicy.visit(level3ScreenPositive.getDataColumn()));
    assertNull("others' level 3 screens overlapping hits screen result not visible", entityViewPolicy.visit(level3ScreenPositive.getDataColumn().getScreenResult()));
  }

  private void setCurrentUser(ScreensaverUser user)
  {
    entityViewPolicy = new IccblEntityViewPolicy(user, genericEntityDao);
  }
  
  public void testLibraryPermissions() 
  {
    ScreeningRoomUser[] users = new ScreeningRoomUser[3];
    AdministratorUser[] admUsers = new AdministratorUser[1];
    
    users[0] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    users[1] = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS); 
    users[2] = makeUserWithRoles(true, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    Lab lab = new Lab((LabHead) users[2]);
    users[0].setLab(lab);
    genericEntityDao.saveOrUpdateEntity(users[0]);

    //user with library_admin has to be an Administrator user otherwise error on validation. 
    admUsers[0] = new AdministratorUser("Joe", "Admin");
    genericEntityDao.saveOrUpdateEntity(admUsers[0]);
    admUsers[0].addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);

    Library library = new Library(admUsers[0],
                                  "library 1",
                                  "lib1",
                                  ScreenType.RNAI,
                                  LibraryType.COMMERCIAL,
                                  100001,
                                  100002,
                                  PlateSize.WELLS_384);
    library.setOwner(users[0]);
    libraryCreator.createWells(library);
    genericEntityDao.saveOrUpdateEntity(library);

    genericEntityDao.flush();
    genericEntityDao.clear();

    //library = genericEntityDao.findEntityByProperty(Library.class, "shortName","lib1" );
    setCurrentUser(users[0]);//genericEntityDao.findEntityById(ScreensaverUser.class, users[0].getEntityId()));
    assertNotNull("Owner can view (validation) library", entityViewPolicy.visit(library));

    setCurrentUser(users[1]);//genericEntityDao.findEntityById(ScreensaverUser.class, users[1].getEntityId()));
    assertNull("Non-owner cannot view Validation Library", entityViewPolicy.visit(library));

    /*
     * TODO ADD CHECK WHEN "NO SESSION" error is solved
     * setCurrentUser(genericEntityDao.findEntityById(ScreensaverUser.class, users[2].getEntityId()));
     * assertNotNull("Library head of owner can view (validation) library", entityViewPolicy.visit(library);
     */

    setCurrentUser(genericEntityDao.findEntityById(ScreensaverUser.class, admUsers[0].getEntityId()));
    assertNotNull("LibrarieAdmin can view (validation) library", entityViewPolicy.visit(library));
  }
}
