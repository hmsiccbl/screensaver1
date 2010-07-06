// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
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

/**
 * Tests WedDataAccessPolicy implementation, as well as Hibernate interceptor-based
 * mechanism for setting "restricted" flag on entities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class IccblEntityViewPolicyTest extends AbstractTransactionalSpringContextTests
{
  // static members

  private static Logger log = Logger.getLogger(IccblEntityViewPolicyTest.class);


  // instance data members
  
  protected GenericEntityDAO genericEntityDao;
  protected ScreenResultsDAO screenResultsDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;
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


  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test-security.xml" };
  }

  public IccblEntityViewPolicyTest() 
  {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  private void initializeDataForDataSharingLevelTests() 
  {
    initializeMe();

    affiliate = new LabHead("Lab", "Affiliate", null);
    stranger = new ScreeningRoomUser("Strange", "Screener");
    genericEntityDao.persistEntity(affiliate);
    genericEntityDao.persistEntity(stranger);
    
    Library libForMutualPositives = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(libForMutualPositives);
    Library libForNonMutualPositives = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(libForNonMutualPositives);
    
    // note: "my" screens will be associated with the "me" user in individual test methods
    
    myLevel0Screen = MakeDummyEntities.makeDummyScreen(0, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(myLevel0Screen, libForMutualPositives);
    myLevel0Screen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);

    myLevel1Screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(myLevel1Screen, libForMutualPositives);
    myLevel1Screen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);

    myLevel2Screen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(myLevel2Screen, libForMutualPositives);
    myLevel2Screen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    myLevel3Screen = MakeDummyEntities.makeDummyScreen(3, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(myLevel3Screen, libForMutualPositives);
    myLevel3Screen.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    
    genericEntityDao.persistEntity(myLevel0Screen);
    genericEntityDao.persistEntity(myLevel1Screen);
    genericEntityDao.persistEntity(myLevel2Screen);
    genericEntityDao.persistEntity(myLevel3Screen);

    othersLevel0ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(100, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel0ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel0ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    genericEntityDao.persistEntity(othersLevel0ScreenWithNonOverlappingHits);

    othersLevel1ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(110, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel1ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel1ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    genericEntityDao.persistEntity(othersLevel1ScreenWithNonOverlappingHits);

    othersLevel1ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(111, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel1ScreenWithOverlappingHits, libForMutualPositives);
    othersLevel1ScreenWithOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    genericEntityDao.persistEntity(othersLevel1ScreenWithOverlappingHits);

    othersLevel2ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(120, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel2ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel2ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    genericEntityDao.persistEntity(othersLevel2ScreenWithNonOverlappingHits);

    othersLevel2ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(121, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel2ScreenWithOverlappingHits, libForMutualPositives);
    othersLevel2ScreenWithOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);
    genericEntityDao.persistEntity(othersLevel2ScreenWithOverlappingHits);

    othersLevel3ScreenWithNonOverlappingHits = MakeDummyEntities.makeDummyScreen(130, ScreenType.SMALL_MOLECULE);
    MakeDummyEntities.makeDummyScreenResult(othersLevel3ScreenWithNonOverlappingHits, libForNonMutualPositives);
    othersLevel3ScreenWithNonOverlappingHits.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    genericEntityDao.persistEntity(othersLevel3ScreenWithNonOverlappingHits);

    othersLevel3ScreenWithOverlappingHits = MakeDummyEntities.makeDummyScreen(131, ScreenType.SMALL_MOLECULE);
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
        if (!entityViewPolicy.visit(user)) {
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
        if (!entityViewPolicy.visit(user)) {
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

  public void testRnaiScreenPermissions()
  {
    initializeMe();
    me.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENS);
    
    ScreeningRoomUser rnaiUser = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENS);
    ScreeningRoomUser smallMoleculeUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    ScreeningRoomUser smallMoleculeRnaiUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreensaverUserRole.RNAI_SCREENS);

    Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    rnaiScreen.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    rnaiScreen.createScreenResult();
    rnaiScreen.createCherryPickRequest((AdministratorUser) rnaiScreen.getCreatedBy());
    rnaiScreen.createLibraryScreening((AdministratorUser) rnaiScreen.getCreatedBy(), rnaiUser, new LocalDate());

    Screen publicRnaiScreen = MakeDummyEntities.makeDummyScreen(10, ScreenType.RNAI);
    publicRnaiScreen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publicRnaiScreen.createScreenResult();
    
    Screen smallMoleculeScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    smallMoleculeScreen.createScreenResult();
    smallMoleculeScreen.createCherryPickRequest((AdministratorUser) smallMoleculeScreen.getCreatedBy());
    smallMoleculeScreen.createLibraryScreening((AdministratorUser) rnaiScreen.getCreatedBy(), rnaiUser, new LocalDate());

    Screen publicSmallMoleculeScreen = MakeDummyEntities.makeDummyScreen(20, ScreenType.SMALL_MOLECULE);
    publicSmallMoleculeScreen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publicSmallMoleculeScreen.createScreenResult();
    
    rnaiScreen.setLeadScreener(rnaiUser);
    smallMoleculeScreen.setLeadScreener(smallMoleculeUser);
    rnaiScreen.addCollaborator(smallMoleculeRnaiUser);
    smallMoleculeScreen.addCollaborator(smallMoleculeRnaiUser);
    
    genericEntityDao.saveOrUpdateEntity(me);
    genericEntityDao.saveOrUpdateEntity(rnaiUser);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeUser);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeRnaiUser);
    genericEntityDao.saveOrUpdateEntity(rnaiScreen);
    genericEntityDao.saveOrUpdateEntity(publicRnaiScreen);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeScreen);
    genericEntityDao.saveOrUpdateEntity(publicSmallMoleculeScreen);
    genericEntityDao.flush();
    
    // TODO: we should follow the comprehensive testing pattern we use for SM screens
    
    // TODO: RNAi screeners can only see other non-public RNAi screens if they have deposited data
    
    setCurrentUser(rnaiUser);
    assertTrue("rnai user is not restricted from own rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertTrue("rnai user is not restricted from own rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertTrue("rnai user is not restricted from own rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertTrue("rnai user is not restricted from own rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    setCurrentUser(me);
    assertTrue("rnai user is not restricted from others' rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertFalse("rnai user is restricted from others' rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertFalse("rnai user is restricted from others' rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertFalse("rnai user is restricted from others' rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertFalse("rnai user is restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertFalse("rnai user is restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    assertTrue("rnai user is not restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertTrue("rnai user is not restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
    setCurrentUser(smallMoleculeUser);
    assertFalse("small molecule user is restricted from others' rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertFalse("small molecule user is restricted from others' rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertFalse("small molecule user is restricted from rnai others' rnai screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertFalse("small molecule user is restricted from rnai others' rnai screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    // note: next 2 assertions have nothing to do with RNAi tests, and are redundant with the more comprehensive SM tests, elsewhere, but we include anyway
    assertTrue("small molecule user is not restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertTrue("small molecule user is not restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    assertFalse("small molecule user is restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertFalse("small molecule user is restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
    setCurrentUser(smallMoleculeRnaiUser);
    assertTrue("small molecule+rnai user is not restricted from own rnai screen", entityViewPolicy.visit(rnaiScreen));
    assertTrue("small molecule+rnai user is not restricted from own rnai screen result", entityViewPolicy.visit(rnaiScreen.getScreenResult()));
    assertTrue("small molecule+rnai user is not restricted from rnai own screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertTrue("small molecule+rnai user is not restricted from rnai own screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertTrue("small molecule+rnai user is not restricted from rnai own screen CPRs", entityViewPolicy.visit((RNAiCherryPickRequest) rnaiScreen.getCherryPickRequests().iterator().next()));
    assertTrue("small molecule+rnai user is not restricted from rnai own screen lab activities", entityViewPolicy.visit((LibraryScreening) rnaiScreen.getLabActivities().iterator().next()));
    assertTrue("small molecule+rnai user is not restricted from others' public small molecule screen", entityViewPolicy.visit(publicSmallMoleculeScreen));
    assertTrue("small molecule+rnai user is not restricted from others' public small molecule screen result", entityViewPolicy.visit(publicSmallMoleculeScreen.getScreenResult()));
    assertTrue("small molecule+rnai user is not restricted from others' public rnai screen", entityViewPolicy.visit(publicRnaiScreen));
    assertTrue("small molecule+rnai user is not restricted from others' public rnai screen result", entityViewPolicy.visit(publicRnaiScreen.getScreenResult()));
  }
  
  public void testMarcusAdminRestrictions()
  {
    AdministratorUser dataEntryAdmin = new AdministratorUser("DataEntry", "Admin", "", "", "", "", null, "");
    AdministratorUser marcusAdmin = new AdministratorUser("Marcus", "Admin", "", "", "", "", null, "");
    marcusAdmin.addScreensaverUserRole(ScreensaverUserRole.MARCUS_ADMIN);
    AdministratorUser normalAdmin = new AdministratorUser("Normal", "Admin", "", "", "", "", null, "");
    normalAdmin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
    Screen nonMarcusScreen = MakeDummyEntities.makeDummyScreen(2);
    nonMarcusScreen.createCherryPickRequest((AdministratorUser) nonMarcusScreen.getCreatedBy());
    nonMarcusScreen.createLibraryScreening(dataEntryAdmin, nonMarcusScreen.getLeadScreener(), new LocalDate());
    nonMarcusScreen.createScreenResult();
    Screen marcusScreen = MakeDummyEntities.makeDummyScreen(1);
    FundingSupport marcusFundingSupport = new FundingSupport(IccblEntityViewPolicy.MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME);
    marcusScreen.addFundingSupport(marcusFundingSupport);
    marcusScreen.createCherryPickRequest((AdministratorUser) marcusScreen.getCreatedBy());
    marcusScreen.createLibraryScreening(dataEntryAdmin, marcusScreen.getLeadScreener(), new LocalDate());
    marcusScreen.createScreenResult();
    genericEntityDao.persistEntity(marcusFundingSupport);
    genericEntityDao.persistEntity(marcusAdmin);
    genericEntityDao.persistEntity(normalAdmin);
    genericEntityDao.persistEntity(marcusScreen);
    genericEntityDao.persistEntity(nonMarcusScreen);
    genericEntityDao.flush();

    
    setCurrentUser(marcusAdmin);
    assertTrue(entityViewPolicy.visit(marcusScreen));
    assertTrue(entityViewPolicy.visit(marcusScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) marcusScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) marcusScreen.getCherryPickRequests().iterator().next()));
    assertFalse(entityViewPolicy.visit(nonMarcusScreen));
    assertFalse(entityViewPolicy.visit(nonMarcusScreen.getScreenResult()));
    assertFalse(entityViewPolicy.visit((LibraryScreening) nonMarcusScreen.getLabActivities().iterator().next()));
    assertFalse(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonMarcusScreen.getCherryPickRequests().iterator().next()));

    setCurrentUser(normalAdmin);
    assertTrue(entityViewPolicy.visit(marcusScreen));
    assertTrue(entityViewPolicy.visit(marcusScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) marcusScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) marcusScreen.getCherryPickRequests().iterator().next()));
    assertTrue(entityViewPolicy.visit(nonMarcusScreen));
    assertTrue(entityViewPolicy.visit(nonMarcusScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) nonMarcusScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonMarcusScreen.getCherryPickRequests().iterator().next()));
  }
  
  public void testGrayAdminRestrictions()
  {
    AdministratorUser dataEntryAdmin = new AdministratorUser("DataEntry", "Admin", "", "", "", "", null, "");
    AdministratorUser grayAdmin = new AdministratorUser("Gray", "Admin", "", "", "", "", null, "");
    grayAdmin.addScreensaverUserRole(ScreensaverUserRole.GRAY_ADMIN);
    AdministratorUser normalAdmin = new AdministratorUser("Normal", "Admin", "", "", "", "", null, "");
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
    assertTrue(entityViewPolicy.visit(grayScreen));
    assertTrue(entityViewPolicy.visit(grayScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) grayScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) grayScreen.getCherryPickRequests().iterator().next()));
    assertFalse(entityViewPolicy.visit(nonGrayScreen));
    assertFalse(entityViewPolicy.visit(nonGrayScreen.getScreenResult()));
    assertFalse(entityViewPolicy.visit((LibraryScreening) nonGrayScreen.getLabActivities().iterator().next()));
    assertFalse(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonGrayScreen.getCherryPickRequests().iterator().next()));

    setCurrentUser(normalAdmin);
    assertTrue(entityViewPolicy.visit(grayScreen));
    assertTrue(entityViewPolicy.visit(grayScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) grayScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) grayScreen.getCherryPickRequests().iterator().next()));
    assertTrue(entityViewPolicy.visit(nonGrayScreen));
    assertTrue(entityViewPolicy.visit(nonGrayScreen.getScreenResult()));
    assertTrue(entityViewPolicy.visit((LibraryScreening) nonGrayScreen.getLabActivities().iterator().next()));
    assertTrue(entityViewPolicy.visit((SmallMoleculeCherryPickRequest) nonGrayScreen.getCherryPickRequests().iterator().next()));
  }
  
  public void testSilencingReagentSequenceRestriction()
  {
    Library library = new Library("rnai library", "rnai lib",
                                  ScreenType.RNAI, LibraryType.COMMERCIAL,
                                  1, 1);
    new TestDataFactory().newInstance(LibraryContentsVersion.class, library);
    SilencingReagent reagent = 
      library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createSilencingReagent(new ReagentVendorIdentifier("vendor", "sirnai1"), SilencingReagentType.SIRNA, "ACTG");

    AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);

    ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User");
    screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    screener.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENS);

    setCurrentUser(admin);
    assertTrue("admin can access SilencingReagent.sequence", entityViewPolicy.isAllowedAccessToSilencingReagentSequence(reagent));
  }
  
  public void testChemDiv6Restriction()
  {
    TestDataFactory dataFactory = new TestDataFactory();

    Library chemDiv6Library = new Library("ChemDiv6",
                                          "ChemDiv6",
                                          ScreenType.SMALL_MOLECULE,
                                          LibraryType.COMMERCIAL,
                                          1,
                                          1);
    dataFactory.newInstance(LibraryContentsVersion.class, chemDiv6Library);
    SmallMoleculeReagent chemDiv6Reagent = chemDiv6Library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", "Cdiv0001"), "molfile", "smiles", "inchi", new BigDecimal(1), new BigDecimal(1), new MolecularFormula("CCC")); 

    Library notCDiv6Library = new Library("NotChemDiv6",
                                          "NotChemDiv6",
                                          ScreenType.SMALL_MOLECULE,
                                          LibraryType.COMMERCIAL,
                                          2,
                                          2);
    dataFactory.newInstance(LibraryContentsVersion.class, notCDiv6Library);
    SmallMoleculeReagent notChemDiv6Reagent = notCDiv6Library.createWell(new WellKey(2, 0, 0), LibraryWellType.EXPERIMENTAL).createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", "notCdiv0001"), "molfile", "smiles", "inchi", new BigDecimal(1), new BigDecimal(1), new MolecularFormula("CCC")); 

    AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);

    ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User");
    screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    screener.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    
    genericEntityDao.persistEntity(chemDiv6Library);
    genericEntityDao.persistEntity(notCDiv6Library);
    setComplete();
    endTransaction();
    
    startNewTransaction();
    setCurrentUser(admin);
    assertTrue("admin can access ChemDiv6 reagent", entityViewPolicy.visit(chemDiv6Reagent));
    assertTrue("admin can access non-ChemDiv6 reagent", entityViewPolicy.visit(notChemDiv6Reagent));
    setCurrentUser(screener);
    assertFalse("screener cannot access ChemDiv6 reagent", entityViewPolicy.visit(chemDiv6Reagent));
    assertTrue("screener can access non-ChemDiv6 reagent", entityViewPolicy.visit(notChemDiv6Reagent));
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
    initializeDataForDataSharingLevelTests();
    me.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);

    makeMeHaveNoScreens();
    assertAllOthersNonPublicScreensNotVisible();
    assertOthersPublicScreensVisible();

    // all of my screens have screen result data
    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions();

    // assert that a level 1 user without deposited data in level 0 or 1 screens is to be treated same as a level 3 user
    // note: only my level 0 and level 1 screens's data count as "deposited data"
    // TODO: should test in reverse order, too
    screenResultsDao.deleteScreenResult(myLevel1Screen.getScreenResult());
    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions();

    screenResultsDao.deleteScreenResult(myLevel0Screen.getScreenResult());
    genericEntityDao.flush();
    entityViewPolicy.update();
    // now "I" have no more deposited data in level 0/1 screens, but still do in my level 2/3 screens
    doTestSmallMoleculeUserLevel3ScreenPermissions();

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
    doTestSmallMoleculeUserLevel3ScreenPermissions();
  }

  public void testSmallMoleculeUserLevel2ScreenPermissions()
  {
    initializeDataForDataSharingLevelTests();
    me.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    
    makeMeHaveNoScreens();
    assertAllOthersNonPublicScreensNotVisible();
    assertOthersPublicScreensVisible();

    makeMeLabHeadOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeLeadScreenerOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
    makeMeCollaboratorOfMyScreens();
    assertLevel1Or2UserScreenPermissions();
  }
  
  public void testSmallMoleculeUserLevel3ScreenPermissions()
  {
    initializeDataForDataSharingLevelTests();
    me.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    
    doTestSmallMoleculeUserLevel3ScreenPermissions();
  }

  private void doTestSmallMoleculeUserLevel3ScreenPermissions()
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

  private void assertLevel1Or2UserScreenPermissions()
  {
    assertMyScreensVisible();
    assertOthersPublicScreensVisible();
    
    // level 1 screeners can see details of other level 1 screens, regardless of overlapping hits
    boolean expectedVisible = me.getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    
    assertTrue("others' level 1 screen with non-overlapping hits visibility", entityViewPolicy.visit(othersLevel1ScreenWithNonOverlappingHits));
    assertEquals("others' level 1 screen with non-overlapping hits, details visibility", expectedVisible, entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel1ScreenWithNonOverlappingHits));
    // TODO: we also need to test DataColumn access permissions
    assertEquals("others' level 1 screen with non-overlapping hits, non-overlapping hits visibility", expectedVisible, entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithNonOverlappingHits)));

    assertTrue("others' level 1 screen with overlapping hits visible", entityViewPolicy.visit(othersLevel1ScreenWithOverlappingHits));
    assertEquals("others' level 1 screen with overlapping hits, details visibility", expectedVisible, entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel1ScreenWithOverlappingHits));
    assertTrue("others' level 1 screen with overlapping hits, overlapping hits visible", entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithOverlappingHits)));

    assertTrue("others' level 2 screen with non-overlapping hits visible", entityViewPolicy.visit(othersLevel2ScreenWithNonOverlappingHits));
    assertFalse("others' level 2 screen with non-overlapping hits visible, but not details", entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel2ScreenWithNonOverlappingHits));
    assertFalse("others' level 2 screen with non-overlapping hits, non-overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithNonOverlappingHits)));

    assertTrue("others' level 2 screen with overlapping hits visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
    assertFalse("others' level 2 screen with overlapping hits visible, but not details", entityViewPolicy.isAllowedAccessToScreenDetails(othersLevel2ScreenWithOverlappingHits));
    assertTrue("others' level 2 screen with overlapping hits, overlapping hits visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits)));
    
    // my level 0, 1 and 2 screens count towards overlapping hit viewing, but not my level 3 screens
    // TODO: this is NOT testing what it purports to test, since othersLevel2ScreenWithOverlappingHits also overlaps my 0,1,2 screens!
//    assertFalse("others' level 2 screen with overlapping hits, overlapping only my level 3 screen, not visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
//    assertFalse("others' level 2 screen with overlapping hits, overlapping only my level 3 screen, overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits)));

    assertFalse("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithNonOverlappingHits));
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
    assertTrue("others mutual positives screen is visible (sanity check)", entityViewPolicy.visit(othersMutualPositivesScreen));
    assertTrue("others non-mutual positives screen is visible, but not details", 
               entityViewPolicy.visit(othersNonMutualPositivesScreen) && !entityViewPolicy.isAllowedAccessToScreenDetails(othersNonMutualPositivesScreen));
    setCurrentUser(othersNonMutualPositivesScreen.getLabHead());
    assertTrue("my non-mutual positives screen details are not visible to others", 
               entityViewPolicy.visit(myScreen) && !entityViewPolicy.isAllowedAccessToScreenDetails(myScreen));
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
    assertFalse("non-mutual positive result value is not visible", entityViewPolicy.visit(nonMutualPositive));
    assertTrue("mutual positive result value is visible", entityViewPolicy.visit(mutualPositive));
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
    assertTrue("my level 0 screen visible", entityViewPolicy.visit(myLevel0Screen));
    assertTrue("my level 1 screen visible", entityViewPolicy.visit(myLevel1Screen));
    assertTrue("my level 2 screen visible", entityViewPolicy.visit(myLevel2Screen));
    assertTrue("my level 3 screen visible", entityViewPolicy.visit(myLevel3Screen));
  }
  
  private void assertOthersPublicScreensVisible()
  {
    assertTrue("others' level 0 screens visible", entityViewPolicy.visit(othersLevel0ScreenWithNonOverlappingHits));
  }

  private void assertAllOthersNonPublicScreensNotVisible()
  {
    assertFalse("others' level 1 screens not visible", entityViewPolicy.visit(othersLevel1ScreenWithOverlappingHits));
    assertFalse("others' level 1 screens not visible", entityViewPolicy.visit(othersLevel1ScreenWithNonOverlappingHits));
    assertFalse("others' level 1 screens overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel1ScreenWithOverlappingHits)));
    assertFalse("others' level 2 screens not visible", entityViewPolicy.visit(othersLevel2ScreenWithOverlappingHits));
    assertFalse("others' level 2 screens not visible", entityViewPolicy.visit(othersLevel2ScreenWithNonOverlappingHits));
    assertFalse("others' level 2 screens overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel2ScreenWithOverlappingHits)));
    assertFalse("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithOverlappingHits));
    assertFalse("others' level 3 screens not visible", entityViewPolicy.visit(othersLevel3ScreenWithNonOverlappingHits));
    assertFalse("others' level 3 screens overlapping hits not visible", entityViewPolicy.visit(findSomePositive(othersLevel3ScreenWithOverlappingHits)));
  }

  private void setCurrentUser(ScreensaverUser user)
  {
    entityViewPolicy = new IccblEntityViewPolicy(user, genericEntityDao);
  }
  
  public void testLibraryPermissions() 
  {
    ScreeningRoomUser[] users = new ScreeningRoomUser[3];
    AdministratorUser[] admUsers = new AdministratorUser[1];
    
    users[0] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENS); 
    users[1] = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS); 
    users[2] = makeUserWithRoles(true, ScreensaverUserRole.RNAI_SCREENS); 
    Lab lab = new Lab((LabHead) users[2]);
    users[0].setLab(lab);
    genericEntityDao.saveOrUpdateEntity(users[0]);

    //user with library_admin has to be an Administrator user otherwise error on validation. 
    admUsers[0] = new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", "");
    genericEntityDao.saveOrUpdateEntity(admUsers[0]);
    admUsers[0].addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);

    Library library = new Library("library 1",
                                  "lib1",
                                  ScreenType.RNAI,
                                  LibraryType.COMMERCIAL,
                                  100001,
                                  100002);
    library.setOwner(users[0]);
    librariesDao.loadOrCreateWellsForLibrary(library);
    genericEntityDao.saveOrUpdateEntity(library);
    //genericEntityDao.flush();

    setComplete();
    endTransaction();
    startNewTransaction();

    //library = genericEntityDao.findEntityByProperty(Library.class, "shortName","lib1" );
    setCurrentUser(users[0]);//genericEntityDao.findEntityById(ScreensaverUser.class, users[0].getEntityId()));
    assertTrue("Owner can view (validation) library", entityViewPolicy.visit(library));

    setCurrentUser(users[1]);//genericEntityDao.findEntityById(ScreensaverUser.class, users[1].getEntityId()));
    assertFalse("Non-owner cannot view Validation Library", entityViewPolicy.visit(library));

/*  TODO ADD CHECK WHEN "NO SESSION" error is solved 
        setCurrentUser(genericEntityDao.findEntityById(ScreensaverUser.class, users[2].getEntityId()));
        assertTrue("Library head of owner can view (validation) library", entityViewPolicy.visit(library);*/

    setCurrentUser(genericEntityDao.findEntityById(ScreensaverUser.class, admUsers[0].getEntityId()));
    assertTrue("LibrarieAdmin can view (validation) library", entityViewPolicy.visit(library));
  }
}
