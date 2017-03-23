// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screenresult;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.policy.IccblEntityViewPolicy;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter.ConfirmationReport;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class ScreenResultReporterTest extends AbstractSpringPersistenceTest
{
  private static final Logger log = Logger.getLogger(ScreenResultReporterTest.class);

  @Autowired
  public ScreenResultReporter screenResultReporter;
  @Autowired
  public LibraryContentsVersionManager libraryContentsVersionManager;
  @Resource
  private EntityViewPolicy entityViewPolicy;

  private SilencingReagent _poolReagent, _poolReagentUnScreened;
  public List<SilencingReagent> _duplexReagents = Lists.newArrayList();
  public List<Well> _duplexWells = Lists.newArrayList();
  public Screen _screenRnai0;
  public Screen _screenRnai1;
  public Screen _screenRnai2;
  public Screen _screenRnai3;
  public Screen _screenRnai4;
  public Screen _screenRnai5;
  public Screen _screenRnaiPool;
  public Well _poolWell;
  public Well _poolWell1;
  public Screen _study;
  public int[] _binArray = new int[5]; // store the counts as we create them, for testing

  public AdministratorUser _admin = null;
  public LabHead _labHead = null;
  public List<String> _sequences = Lists.newArrayList("GAUGAACAGACUCCAAUUC", "GAUGAAGAGCCUAUUGAAG", "GAGCUUACAACCUGCCUUA", "GAACAGACUCCAAUUCAUA");


  public ScreenResultReporterTest()
  {
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
  }

  private void setupInTransaction_createAdmin()
  {
    _admin = dataFactory.newInstance(AdministratorUser.class);
    _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.persistEntity(_admin);
    _labHead = dataFactory.newInstance(LabHead.class);
    genericEntityDao.persistEntity(_labHead);
  }

  private void setupInTransaction_createStudy()
  {
    LabHead labHead = dataFactory.newInstance(LabHead.class);
    _study = new Screen(_admin,
                        ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_CONFIRMATION_SUMMARY,
                        labHead,
                        labHead,
                        ScreenType.RNAI,
                        StudyType.IN_SILICO,
                        ProjectPhase.ANNOTATION,
                        "Title");
    _study.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    _study.setSummary("Summary");

    genericEntityDao.persistEntity(_study);
  }

  private void setupInTransaction_createReagents()
  {
    // Create two duplex libraries to be associated with the pool library, one that has confirmation results, one that does not 
    Library duplexLibrary = dataFactory.newInstance(Library.class);
    duplexLibrary.setLibraryName("duplexLibrary");

    Library duplexLibrary1 = dataFactory.newInstance(Library.class);
    duplexLibrary1.setLibraryName("duplexLibrary1");

    flushAndClear();

    // Create the Wells
    // the confirming wells
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary, _admin, "");
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary1, _admin, "");


    duplexLibrary = new EntityInflator<Library>(genericEntityDao,
                                                genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary"),
                                                false).need(Library.wells).need(Library.contentsVersions).inflate();
    duplexLibrary.setStartPlate(1);
    duplexLibrary.setEndPlate(1);
    duplexLibrary.setScreenType(ScreenType.RNAI);
    
    for (int i = 0; i < _sequences.size(); i++) {
      Well well = duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A0" + (i + 1)), LibraryWellType.EXPERIMENTAL);
      well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1." + i), SilencingReagentType.SIRNA, _sequences.get(i));
      _duplexWells.add(well);
    }

    //// create the non-confirming wells 
    duplexLibrary1 = new EntityInflator<Library>(genericEntityDao,
                                                 genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary1"),
                                                 false).need(Library.wells).need(Library.contentsVersions).inflate();
    duplexLibrary1.setStartPlate(10);
    duplexLibrary1.setEndPlate(10);
    duplexLibrary1.setScreenType(ScreenType.RNAI);

    for (int i = 0; i < _sequences.size(); i++) {
      Well well = duplexLibrary1.createWell(new WellKey(duplexLibrary1.getStartPlate(), "A0" + (i + 1)), LibraryWellType.EXPERIMENTAL);
      well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1." + i), SilencingReagentType.SIRNA, _sequences.get(i) +
        "X");
      //_duplexWells.add(well);
    }
    flushAndClear();

    libraryContentsVersionManager.releaseLibraryContentsVersion(duplexLibrary.getLatestContentsVersion(), _admin);
    libraryContentsVersionManager.releaseLibraryContentsVersion(duplexLibrary1.getLatestContentsVersion(), _admin);

    // Now create the pool library, and associtate the duplex wells to itduplex
    List<Well> duplexWells = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary);
    List<Well> duplexWells1 = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary1);

    Library poolLibrary = dataFactory.newInstance(Library.class);
    poolLibrary.setLibraryName("Pool");
    poolLibrary.setPool(true);
    poolLibrary.setStartPlate(2);
    poolLibrary.setEndPlate(2);
    poolLibrary.setScreenType(ScreenType.RNAI);
    poolLibrary.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
    _poolWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL);
    String poolSequence = _sequences.get(0);
    for (int k = 1; k < _sequences.size(); k++)
      poolSequence += "," + _sequences.get(k);
    
    _poolReagent = _poolWell.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1"), SilencingReagentType.SIRNA, poolSequence);
    _poolReagent.withDuplexWell(duplexWells.get(0));
    _poolReagent.withDuplexWell(duplexWells.get(1));
    _poolReagent.withDuplexWell(duplexWells.get(2));
    _poolReagent.withDuplexWell(duplexWells.get(3));

    // create an unconfirmed poolwell
    _poolWell1 = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL);

    _poolReagentUnScreened = _poolWell1.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1a"), SilencingReagentType.SIRNA, poolSequence);
    _poolReagentUnScreened.withDuplexWell(duplexWells1.get(0));
    _poolReagentUnScreened.withDuplexWell(duplexWells1.get(1));
    _poolReagentUnScreened.withDuplexWell(duplexWells1.get(2));
    _poolReagentUnScreened.withDuplexWell(duplexWells1.get(3));

    flushAndClear();

    libraryContentsVersionManager.releaseLibraryContentsVersion(poolLibrary.getLatestContentsVersion(), _admin);

    poolLibrary =
        genericEntityDao.findEntityByProperty(Library.class,
                                              "libraryName",
                                              "Pool",
                                              true,
                                              Library.wells.to(Well.latestReleasedReagent).to(SilencingReagent.duplexWells).to(Well.latestReleasedReagent));

    assertEquals(duplexLibrary.getWells(),
                 ((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexWells());
    assertEquals(Sets.newHashSet(_sequences),
                 Sets.newHashSet(Iterables.transform(((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexSilencingReagents(),
                                                     new Function<SilencingReagent,String>() {
                                                       public String apply(SilencingReagent sr)
                                                       {
                                                         return sr.getSequence();
                                                       }
                                                     })));
    for (String sequence : _sequences) {
      SilencingReagent duplexReagent = genericEntityDao.findEntityByProperty(SilencingReagent.class, "sequence", sequence);
      assertNotNull("sequnce not found: " + sequence, duplexReagent);
      _duplexReagents.add(duplexReagent);
    }
  }

  private void setupInTransaction_createScreenResults()
  {
    // create a pool screen (not needed for this test, useful for ui)
    int screenNumber = 1;

    _screenRnaiPool = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    _screenRnaiPool.setTitle("Pool screen");

    ScreenResult screenResult = _screenRnaiPool.createScreenResult();
    DataColumn col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();

    AssayWell assayWellPool = screenResult.createAssayWell(_poolWell);
    Reagent reagent = assayWellPool.getLibraryWell().getLatestReleasedReagent();
    ResultValue resultValue = col.createBooleanPositiveResultValue(assayWellPool, true, false);
    assertTrue(resultValue.isPositive());

    // create another pool well with no confirmation (not needed for this test, useful for ui)
    assayWellPool = screenResult.createAssayWell(_poolWell1);
    reagent = assayWellPool.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellPool, true, false);
    assertTrue(resultValue.isPositive());

    // create duplex screens
    // TODO: create a screen with all inconclusive

    ///////////////////////////////////////////////
    // create a screen with 1 confirmed positive
    _screenRnai0 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai0.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    // first confirmed positive
    AssayWell assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    Reagent reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());
    _binArray[1]++; // keep track of the count

    ////////////////////////////////////////////
    // create a screen with 3 confirmed positive
    _screenRnai1 = MakeDummyEntities.makeDummyScreen("A10", ScreenType.RNAI, StudyType.IN_VITRO); // set to A10 so that sort may be tested
    screenResult = _screenRnai1.createScreenResult();

    // create confirmed positive result
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1,
                                                                     ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                     false);
    assertTrue(resultValue.isPositive());
    //create a second confirmed positive for this screen
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(2));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1,
                                                                     ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                     false); //create a second confirmed positive for this screen
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(3));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1,
                                                                     ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                     false);
    assertTrue(resultValue.isPositive());
    _binArray[3]++; // keep track of the count

    //////////////////////////////////////////////
    // create a screen with no confirmed positives - should not show up in the results at all
    // create a boolean positive indicator that will not be counted
    _screenRnai2 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai2.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai1, true, false);
    assertTrue(resultValue.isPositive());

    /////////////////////////////////////////////
    // create a screen with one confirmed negative
    // create a confirmed negative
    _screenRnai3 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai3.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(1));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.FALSE_POSITIVE, false);
    assertFalse(resultValue.isPositive());
    _binArray[0]++; // keep track of the count

    ///////////////////////////////////////////////
    // create a screen with 3 confirmed positives
    _screenRnai4 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai4.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    // also create one inconclusive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(2));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.INCONCLUSIVE, false);
    assertFalse(resultValue.isPositive());

    // first confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    // second confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(1));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    // third confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(3));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    _binArray[3]++; // keep track of the count

    ///////////////////////////////////////////////
    // create 4 confirmed positives for this screen
    _screenRnai5 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai5.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    // first confirmed positive//    assayWellRnai1 = genericEntityDao.mergeEntity(assayWellRnai1);// TODO: Confirm this one is needed

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    // first confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(1));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    // second confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(2));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    // third confirmed positive
    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(3));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());
    _binArray[4]++; // keep track of the count

    genericEntityDao.persistEntity(_screenRnaiPool);
    genericEntityDao.persistEntity(_screenRnai0);
    genericEntityDao.persistEntity(_screenRnai1);
    genericEntityDao.persistEntity(_screenRnai2);
    genericEntityDao.persistEntity(_screenRnai3);
    genericEntityDao.persistEntity(_screenRnai4);
    genericEntityDao.persistEntity(_screenRnai5);
  }

  //TODO: migrated from ScreenResultDAOTest; integrate with the above ... -sde4

  AssayWell assayWell1 = null;
  AssayWell assayWell1a = null;
  AssayWell assayWell2 = null;
  AssayWell assayWell2a = null;

  Reagent reagent1 = null;
  Reagent reagent1a = null;
  Reagent reagent2 = null;
  Reagent reagent2a = null;

  AssayWell assayWellRnai1 = null;
  AssayWell assayWellRnai1a = null;
  AssayWell assayWellRnai2 = null;
  AssayWell assayWellRnai2a = null;

  Reagent reagentRnai1 = null;
  Reagent reagentRnai1a = null;
  Reagent reagentRnai2 = null;
  Reagent reagentRnai2a = null;

  ScreeningRoomUser _rnaiUser = null;
  ScreeningRoomUser _smallMoleculeUser = null;
  ScreeningRoomUser _smallMoleculeLevel3User = null;
  ScreeningRoomUser _smallMoleculeRnaiUser = null;

  int crossScreenPositiveCountWell1 = 0;
  int crossScreenCountWell1 = 0;
  int crossScreenPositiveCountWell1Rnai = 0;
  int crossScreenCountWell1Rnai = 0;

  //TODO: migrated from ScreenResultDaoTest; integrate this setup method with the one's listed above -sde4
  public void onSetUpInTransaction_createCrossScreenPositives()
  {
    setupInTransaction_createAdmin();

    String server = "ss.harvard.com"; // note mailinator reduced size of supported addresses
    //_admin = new AdministratorUser("dev", "testaccount");

    _rnaiUser = makeUserWithRoles(false, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    _smallMoleculeUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    _smallMoleculeRnaiUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS,
                                               ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);

    // Create a library  - SM
    int libraryId = 1;
    Library library = MakeDummyEntities.makeDummyLibrary(libraryId++, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(library);
    genericEntityDao.flush();

    Iterator<Well> wellsIter = library.getWells().iterator();
    Well well1 = wellsIter.next();
    Well well2 = wellsIter.next();
    Well well3 = wellsIter.next();

    // Create a library  - RNAi
    Library libraryRnai = MakeDummyEntities.makeDummyLibrary(libraryId++, ScreenType.RNAI, 1);
    genericEntityDao.persistEntity(libraryRnai);
    genericEntityDao.flush();

    wellsIter = libraryRnai.getWells().iterator();
    Well wellRnai1 = wellsIter.next();
    Well wellRnai2 = wellsIter.next();
    Well wellRnai3 = wellsIter.next();

    // Create Screens
    // Create Small Molecule Screens
    int screenFacilityId = 0;

    // Screen1
    Screen screen1 = MakeDummyEntities.makeDummyScreen(screenFacilityId++, ScreenType.SMALL_MOLECULE);
    screen1.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    ScreenResult screenResult = screen1.createScreenResult();
    DataColumn col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();
    // create a positive: positive count: 1
    assayWell1 = screenResult.createAssayWell(well1);
    reagent1 = assayWell1.getLibraryWell().getLatestReleasedReagent();
    ResultValue resultValue = col.createBooleanPositiveResultValue(assayWell1, true, false);
    assertTrue(resultValue.isPositive());
    crossScreenPositiveCountWell1++;
    crossScreenCountWell1++;

    // create a "negative"
    assayWell1a = screenResult.createAssayWell(well3);
    reagent1a = assayWell1a.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWell1a, false, false);
    assertTrue(!resultValue.isPositive());
    
    genericEntityDao.persistEntity(screen1);
    genericEntityDao.flush();

    // Screen2 - smallMoleculeUser's screen (has to have at least one screen with results to see the study,
    // refer to edu.harvard.med.iccbl.screensaver.policy.IccblEntityViewPolicy.userHasQualifiedDepositedSmallMoleculeData()

    Screen screen2 = MakeDummyEntities.makeDummyScreen(screenFacilityId++, ScreenType.SMALL_MOLECULE);
    screen2.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    screen2.setLeadScreener(_smallMoleculeUser);

    screenResult = screen2.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();

    // create a cross-screen negative: (positive count still 2)
    assayWell2a = screenResult.createAssayWell(well1);
    reagent2a = assayWell2a.getLibraryWell().getLatestReleasedReagent();
    assertEquals("Reagent2a should be the same as Reagent1", reagent1, reagent2a);
    resultValue = col.createBooleanPositiveResultValue(assayWell2a, false, false);
    assertTrue(!resultValue.isPositive());
    crossScreenCountWell1++;

    // create a positive
    assayWell2 = screenResult.createAssayWell(well2);
    reagent2 = assayWell1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWell2, true, false);
    assertTrue(resultValue.isPositive());

    genericEntityDao.persistEntity(screen2);
    genericEntityDao.flush();
      
    // Screen3 - Private screen

    Screen screen3 = MakeDummyEntities.makeDummyScreen(screenFacilityId++, ScreenType.SMALL_MOLECULE);
    screen3.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    //screen3.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    screen3.setLeadScreener(_smallMoleculeUser);

    screenResult = screen3.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();
    // create a cross-screen positive: positive count: 2; note that private screens are included
    assayWell2a = screenResult.createAssayWell(well1);
    reagent2a = assayWell2a.getLibraryWell().getLatestReleasedReagent();
    assertEquals("Reagent2a should be the same as Reagent1", reagent1, reagent2a);
    resultValue = col.createBooleanPositiveResultValue(assayWell2a, true, false);
    assertTrue(resultValue.isPositive());
    crossScreenPositiveCountWell1++;
    crossScreenCountWell1++;

    // create a positive
    assayWell2 = screenResult.createAssayWell(well2);
    reagent2 = assayWell1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWell2, true, false);
    assertTrue(resultValue.isPositive());

    genericEntityDao.persistEntity(screen3);
    genericEntityDao.flush();

    //RNAI screens - do the same 

    Screen screenRnai1 = MakeDummyEntities.makeDummyScreen(screenFacilityId++, ScreenType.RNAI);
    screenResult = screenRnai1.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();
    // create a positive
    assayWellRnai1 = screenResult.createAssayWell(wellRnai1);
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai1, true, false);
    assertTrue(resultValue.isPositive());
    crossScreenPositiveCountWell1Rnai++;
    crossScreenCountWell1Rnai++;

    // create a "negative"
    assayWellRnai1a = screenResult.createAssayWell(wellRnai3);
    reagentRnai1a = assayWellRnai1a.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai1a, false, false);
    assertTrue(!resultValue.isPositive());

    genericEntityDao.persistEntity(screenRnai1);
    genericEntityDao.flush();
    
    Screen screenRnai2 = MakeDummyEntities.makeDummyScreen(screenFacilityId++, ScreenType.RNAI);
    screenResult = screenRnai2.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();
    // create a cross-screen positive
    assayWellRnai2a = screenResult.createAssayWell(wellRnai1);
    reagentRnai2a = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    assertEquals("Reagent2a should be the same as Reagent1", reagentRnai1, reagentRnai2a);
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai2a, true, false);
    assertTrue(resultValue.isPositive());
    crossScreenPositiveCountWell1Rnai++;
    crossScreenCountWell1Rnai++;
    log.info("sm reagent: " + reagent1 + ", studies: " + reagent1.getStudies());
    log.info("rnai reagent: " + reagentRnai1 + ", studies: " + reagentRnai1.getStudies());

    // create a positive
    assayWellRnai2 = screenResult.createAssayWell(wellRnai2);
    reagentRnai2 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai2, true, false);
    assertTrue(resultValue.isPositive());
    
    genericEntityDao.persistEntity(screenRnai2);
    genericEntityDao.flush();
  }

  @Transactional
  public void testDuplexReconfirmationReport()
  {
    setupInTransaction_createAdmin();
    setupInTransaction_createStudy();
    setupInTransaction_createReagents();
    setupInTransaction_createScreenResults();

    flushAndClear();

    _screenRnai0 = genericEntityDao.reloadEntity(_screenRnai0);
    _screenRnai1 = genericEntityDao.reloadEntity(_screenRnai1);
    _screenRnai2 = genericEntityDao.reloadEntity(_screenRnai2);
    _screenRnai3 = genericEntityDao.reloadEntity(_screenRnai3);
    _screenRnai4 = genericEntityDao.reloadEntity(_screenRnai4);
    _screenRnai5 = genericEntityDao.reloadEntity(_screenRnai5);

    // TODO: one way to test may be to create a ReconfirmationReport to compare this result to (using equals)
    ConfirmationReport report = screenResultReporter.getDuplexReconfirmationReport(_poolReagent);

    assertTrue(report.getDuplexReagents().size() == 4);
    assertEquals(Sets.newHashSet(_sequences),
                 Sets.newHashSet(Iterables.transform(report.getDuplexReagents(),
                                                     new Function<SilencingReagent,String>() {
                                                       public String apply(SilencingReagent sr)
                                                       {
                                                         return sr.getSequence();
                                                       }
                                                     })));

    assertEquals(Lists.newArrayList(_screenRnai0, _screenRnai3, _screenRnai4, _screenRnai5, _screenRnai1), report.getScreens()); // Note: ordering is because of the screen_number sort
    assertTrue(report.getResults().containsKey(_screenRnai0));
    assertTrue(report.getResults().containsKey(_screenRnai1));
    assertFalse(report.getResults().containsKey(_screenRnai2));
    assertTrue(report.getResults().containsKey(_screenRnai3));
    assertTrue(report.getResults().containsKey(_screenRnai4));
    assertTrue(report.getResults().containsKey(_screenRnai5));

    assertTrue(report.getResults().get(_screenRnai0).containsKey(_duplexReagents.get(0)));
    assertTrue(report.getResults().get(_screenRnai1).containsKey(_duplexReagents.get(0)));
    assertTrue(report.getResults().get(_screenRnai1).containsKey(_duplexReagents.get(2)));
    assertTrue(report.getResults().get(_screenRnai3).containsKey(_duplexReagents.get(1)));
    assertTrue(report.getResults().get(_screenRnai4).containsKey(_duplexReagents.get(3)));
    assertTrue(report.getResults().get(_screenRnai5).containsKey(_duplexReagents.get(3)));
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai0).get(_duplexReagents.get(0)));
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai1).get(_duplexReagents.get(0)));
    assertEquals(ConfirmedPositiveValue.FALSE_POSITIVE, report.getResults().get(_screenRnai3).get(_duplexReagents.get(1)));
    assertNull(/* ConfirmedPositiveValue.INCONCLUSIVE, */report.getResults().get(_screenRnai4).get(_duplexReagents.get(2)));
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai4).get(_duplexReagents.get(0)));
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai4).get(_duplexReagents.get(1)));
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai4).get(_duplexReagents.get(3)));
    // for 5, just an arbitrary check...
    assertEquals(ConfirmedPositiveValue.CONFIRMED_POSITIVE, report.getResults().get(_screenRnai5).get(_duplexReagents.get(3)));
  }

  @Transactional
  public void testCreateSilencingReagentConfirmedPositiveSummaryStudy()
  {
    setupInTransaction_createAdmin();
    setupInTransaction_createStudy();
    setupInTransaction_createReagents();
    setupInTransaction_createScreenResults();
    // To test
    // 1. create a primary screen with a pool well result
    
    _screenRnai0 = genericEntityDao.reloadEntity(_screenRnai0);
    _screenRnai1 = genericEntityDao.reloadEntity(_screenRnai1);
    _screenRnai2 = genericEntityDao.reloadEntity(_screenRnai2);
    _screenRnai3 = genericEntityDao.reloadEntity(_screenRnai3);
    _screenRnai4 = genericEntityDao.reloadEntity(_screenRnai4);
    _screenRnai5 = genericEntityDao.reloadEntity(_screenRnai5);
    _study = genericEntityDao.reloadEntity(_study);
    assertNotNull(_study);

    screenResultReporter.createSilencingReagentConfirmedPositiveSummary(_study);
    genericEntityDao.flush();

    _study = new EntityInflator<Screen>(genericEntityDao,
                                        _study,
                                        true).need(Screen.reagents).need(Screen.annotationTypes).inflate();

    assertEquals(ImmutableSet.of(ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(0),
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(1),
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(2),
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(3),
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(4),
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_NUMBER_OF_SCREENS,
                                 ScreenResultReporter.DEFAULT_ANNOTATION_NAME_WEIGHTED_AVERAGE),
                 ImmutableSet.copyOf(Iterables.transform(_study.getAnnotationTypes(), AnnotationType.ToName)));
    //fixing this - sde...
    assertEquals(ImmutableSet.of(_poolReagent.getVendorId(), _poolReagentUnScreened.getVendorId()),
                     ImmutableSet.copyOf(Iterables.transform(_study.getReagents(), new Function<Reagent,ReagentVendorIdentifier>() {
                       @Override
                       public ReagentVendorIdentifier apply(Reagent from)
                      {
                        return from.getVendorId();
                      }
                     })));

    AnnotationType[] at_types = new AnnotationType[7];
    for (AnnotationType at : _study.getAnnotationTypes()) {
      at = genericEntityDao.reloadEntity(at, true, AnnotationType.annotationValues);
      log.info("at: " + at.getName());
      for (int i = 0; i < 5; i++)
        if (at.getName().equals(ScreenResultReporter.DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(new Integer(i)))) at_types[i] = at;
      if (at.getName().equals(ScreenResultReporter.DEFAULT_ANNOTATION_NAME_NUMBER_OF_SCREENS)) at_types[5] = at;
      if (at.getName().contains(ScreenResultReporter.DEFAULT_ANNOTATION_NAME_WEIGHTED_AVERAGE)) at_types[6] = at;
      for (Map.Entry<Reagent,AnnotationValue> entry : at.getAnnotationValues().entrySet()) {
        log.info("R" + entry.getKey().getVendorId() + ":" + entry.getValue().getNumericValue());

      }
    }

    int screenedCount = 0;
    for (int i = 0; i < 5; i++)
      screenedCount += _binArray[i];
    float weightedAvg = ScreenResultReporter.ConfirmationReport.getWeightedAverage(_binArray, 2);

    assertEquals(new Double(_binArray[0]), at_types[0].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double(_binArray[1]), at_types[1].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double(_binArray[2]), at_types[2].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double(_binArray[3]), at_types[3].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double(_binArray[4]), at_types[4].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double(screenedCount), at_types[5].getAnnotationValues().get(_poolReagent).getNumericValue());
    assertEquals(new Double("" + weightedAvg), at_types[6].getAnnotationValues().get(_poolReagent).getNumericValue());
  }

  public void testWeightedAverage()
  {
    int decimalPlaces = 2;
    int[] array = { 1 };
    float weightedAverage = 0;
    assertEquals(weightedAverage, ScreenResultReporter.ConfirmationReport.getWeightedAverage(array, decimalPlaces));

    array = new int[] { 1, 1 };
    weightedAverage = 0.5f;
    assertEquals(weightedAverage, ScreenResultReporter.ConfirmationReport.getWeightedAverage(array, decimalPlaces));

    array = new int[] { 1, 0, 0, 3 };
    weightedAverage = (1 * 0 + 0 * 1 + 0 * 2 + 3 * 3) / 4f;
    assertEquals(weightedAverage, ScreenResultReporter.ConfirmationReport.getWeightedAverage(array, decimalPlaces));

    array = new int[] { 1, 1, 1, 1, 1, 1 };
    weightedAverage = (1 * 0 + 1 * 1 + 1 * 2 + 1 * 3 + 1 * 4 + 1 * 5) / (float) (1 + 1 + 1 + 1 + 1 + 1);
    assertEquals(weightedAverage, ScreenResultReporter.ConfirmationReport.getWeightedAverage(array, decimalPlaces));
  }

  @Transactional
  public void testCrossScreenPositivesStudyCreatorSM()
  {
    onSetUpInTransaction_createCrossScreenPositives();

    // Create a SM Study

    ScreenType screenType = ScreenType.SMALL_MOLECULE;
    //    Multiset<Reagent> reagents = screenResultsDao.findScreenPositiveReagentsNotDistinct(screenType);
    //    assertFalse("no SM positives found!", reagents.isEmpty());
    int count = screenResultReporter.createReagentCountStudy(_admin,
                                                             _labHead,
                                                             ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_SM,
                                                             "Title",
                                                             "Summary",
                                                             "Positives",
                                                             "The positives",
                                                             "Overall",
                                                             "The overall",
                                                             screenType)[0];
    assertTrue(count > 0);

    flushAndClear();

    Screen smStudy = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_SM);
    assertNotNull(smStudy);

    smStudy = genericEntityDao.reloadEntity(smStudy, true, Screen.reagents);
    Set<Reagent> studyReagents = smStudy.getReagents();
    assertNotNull(studyReagents);
    assertTrue("no reagents in the study:", !studyReagents.isEmpty());
    assertEquals("should be: 1 each for Wells 1,2,3", 3, studyReagents.size());

    log.info("SM Study - display the annotation values");
    for (Reagent reagent : studyReagents) {
      log.info("reagent: " + reagent.getVendorId());
      for (Map.Entry<AnnotationType,AnnotationValue> entry : reagent.getAnnotationValues().entrySet()) {
        log.info("" + entry.getKey() + ", " + entry.getKey().getName() + ", Study: " +
          ((Screen) entry.getKey().getStudy()).getTitle() + ", " +
          entry.getValue().getNumericValue());
      }
    }

    Reagent studyReagent = assayWell1.getLibraryWell().getReagents().entrySet().iterator().next().getValue();
    log.info("reagent to test: " + studyReagent.getVendorId());
    assertTrue(studyReagents.contains(studyReagent));

    studyReagent = genericEntityDao.reloadEntity(studyReagent, true, Reagent.annotationValues);

    // verify #annotation values: this will be 2 - positives count and overall count
    assertEquals("reagent.getAnnotationValues().size()", 2, studyReagent.getAnnotationValues().size());

    AnnotationValue avPositiveCount = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals("Positives")) {
        avPositiveCount = entry.getValue();
        break;
      }
    }
    assertNotNull(avPositiveCount);
    assertEquals("avPositiveCount is: " + avPositiveCount.getNumericValue(), new Double(crossScreenPositiveCountWell1), avPositiveCount.getNumericValue());

    AnnotationValue avOverallCount = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals("Overall")) {
        avOverallCount = entry.getValue();
        break;
      }
    }
    assertNotNull(avOverallCount);
    assertEquals("avOverallCount is: " + avOverallCount.getNumericValue(), new Double(crossScreenCountWell1), avOverallCount.getNumericValue());

    //verify that the SM user can see it
    setCurrentUser(_smallMoleculeUser);
    assertNotNull("SM user should not be restricted from SM Study", entityViewPolicy.visit(smStudy));

    setCurrentUser(_rnaiUser);
    assertNull("rnai user should be restricted from SM Study", entityViewPolicy.visit(smStudy));
  }

  @Transactional
  public void testCrossScreenPositivesStudyCreatorRNAi()
  {
    onSetUpInTransaction_createCrossScreenPositives();
    // Create a RNAi Study

    ScreenType screenType = ScreenType.RNAI;
    //    reagents = screenResultsDao.findScreenPositiveReagentsNotDistinct(screenType);
    //    assertFalse("no RNAi positives found!", reagents.isEmpty());
    int count = screenResultReporter.createReagentCountStudy(_admin,
                                                             _labHead,
                                                             ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_RNAI,
                                                             "RNAi Study Title",
                                                             "RNAi Study Summary",
                                                             "Positives",
                                                             "The positives",
                                                             "Overall",
                                                             "The overall",
                                                             screenType)[0];
    assertTrue(count > 0);

    ////////////
    //RNAi Study
    ////////////
    flushAndClear();

    Screen rnaiStudy = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_RNAI);
    assertNotNull(rnaiStudy);

    rnaiStudy = genericEntityDao.reloadEntity(rnaiStudy, true, Screen.reagents);
    Set<Reagent> studyReagents = rnaiStudy.getReagents();
    assertNotNull(studyReagents);
    assertTrue("no reagents in the RNAi study:", !studyReagents.isEmpty());
    assertEquals("RNAi Study: should be: 1 reagent for each 3 wells", 3, studyReagents.size());

    log.info("RNAi Study");
    for (Reagent reagent : studyReagents) {
      log.info("reagent: " + reagent.getVendorId());
      for (Map.Entry<AnnotationType,AnnotationValue> entry : reagent.getAnnotationValues().entrySet()) {
        log.info("Annotation: " + entry.getKey() + ", " + entry.getKey().getName() + ", Study: " +
          ((Screen) entry.getKey().getStudy()).getTitle() + ", " +
          entry.getValue().getNumericValue());
      }
    }

    Reagent studyReagent = assayWellRnai1.getLibraryWell().getReagents().entrySet().iterator().next().getValue();
    assertTrue(studyReagents.contains(studyReagent));

    studyReagent = genericEntityDao.reloadEntity(studyReagent, true, Reagent.annotationValues);

    assertEquals("reagent.getAnnotationValues().size()", 2, studyReagent.getAnnotationValues().size());

    AnnotationValue avPositiveCountRnai = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals("Overall")) {
        avPositiveCountRnai = entry.getValue();
        break;
      }
    }
    //avPositiveCount = studyReagent.getAnnotationValues().entrySet().iterator().next().getValue();
    assertEquals("av.getNumericValue() is: " + avPositiveCountRnai.getNumericValue(), avPositiveCountRnai.getNumericValue(), new Double(crossScreenPositiveCountWell1Rnai));
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
    genericEntityDao.persistEntity(user);
    return user;
  }

  private void setCurrentUser(ScreensaverUser user)
  {
    entityViewPolicy = new IccblEntityViewPolicy(user, genericEntityDao);
  }
}
