// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import edu.harvard.med.iccbl.screensaver.io.screens.ScreenPositivesCountStudyCreator;
import edu.harvard.med.iccbl.screensaver.policy.IccblEntityViewPolicy;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenResultDAOTest extends AbstractTransactionalSpringContextTests
{

  private static final Logger log = Logger.getLogger(ScreenResultDAOTest.class);

  protected GenericEntityDAO genericEntityDao;
  protected ScreenResultsDAO screenResultsDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;
  private IccblEntityViewPolicy entityViewPolicy;

  
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

  AdministratorUser admin = null;
  ScreeningRoomUser rnaiUser = null;
  ScreeningRoomUser smallMoleculeUser = null;
  ScreeningRoomUser smallMoleculeLevel3User = null;
  ScreeningRoomUser smallMoleculeRnaiUser = null;
  
  int crossScreenPositiveCountWell1 = 0;
  int crossScreenCountWell1 = 0;
  int crossScreenPositiveCountWell1Rnai = 0;
  int crossScreenCountWell1Rnai = 0;

  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public ScreenResultDAOTest() 
  {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  protected void onSetUpInTransaction_ScreenResults()
  {
    String server = "ss.harvard.com"; // note mailinator reduced size of supported addresses
    admin = new AdministratorUser("dev", "testaccount", "admin@" + server, "", "", "", "dev", "");

    rnaiUser = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENS);
    smallMoleculeUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    smallMoleculeRnaiUser = makeUserWithRoles(false, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS,
                                              ScreensaverUserRole.RNAI_SCREENS);

    // Create a library  - SM
    int libraryId = 1;
    Library library = MakeDummyEntities.makeDummyLibrary(libraryId++, ScreenType.SMALL_MOLECULE, 1);
    Iterator<Well> wellsIter = library.getWells().iterator();
    Well well1 = wellsIter.next();
    Well well2 = wellsIter.next();
    Well well3 = wellsIter.next();

    // Create a library  - RNAi
    Library libraryRnai = MakeDummyEntities.makeDummyLibrary(libraryId++, ScreenType.RNAI, 1);
    wellsIter = libraryRnai.getWells().iterator();
    Well wellRnai1 = wellsIter.next();
    Well wellRnai2 = wellsIter.next();
    Well wellRnai3 = wellsIter.next();
    // Create Screens
    // Create Small Molecule Screens
    int screenNumber = 0;
    
    // Screen1
    Screen screen1 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.SMALL_MOLECULE);
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

    // Screen2 - smallMoleculeUser's screen (has to have at least one screen with results to see the study,
    // refer to edu.harvard.med.iccbl.screensaver.policy.IccblEntityViewPolicy.userHasQualifiedDepositedSmallMoleculeData()
    
    Screen screen2 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.SMALL_MOLECULE);
    screen2.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    screen2.setLeadScreener(smallMoleculeUser);

    screenResult = screen2.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();

    // create a cross-screen negative: (positive count still 2)
    assayWell2a = screenResult.createAssayWell(well1);
    reagent2a = assayWell2a.getLibraryWell().getLatestReleasedReagent();
    assertEquals("Reagent2a should be the same as Reagent1",reagent1,reagent2a);
    resultValue = col.createBooleanPositiveResultValue(assayWell2a, false, false);
    assertTrue(!resultValue.isPositive());
    crossScreenCountWell1++;

    // create a positive
    assayWell2 = screenResult.createAssayWell(well2);
    reagent2 = assayWell1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWell2, true, false);
    assertTrue(resultValue.isPositive());
    
    // Screen3 - Private screen

    Screen screen3 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.SMALL_MOLECULE);
    screen3.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    //screen3.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    screen3.setLeadScreener(smallMoleculeUser);

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
    
    //RNAI screens - do the same 
    
    Screen screenRnai1 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
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

    Screen screenRnai2 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
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

    genericEntityDao.persistEntity(library);
    genericEntityDao.persistEntity(libraryRnai);
    genericEntityDao.persistEntity(screen1);
    genericEntityDao.persistEntity(screen2);
    genericEntityDao.persistEntity(screen3);
    genericEntityDao.persistEntity(screenRnai1);
    genericEntityDao.persistEntity(screenRnai2);
    genericEntityDao.flush();
  }

  public void testCrossScreenPositivesStudyCreator()
  {
    onSetUpInTransaction_ScreenResults();
    setComplete();
    endTransaction();
    startNewTransaction();

    // Create a SM Study
    
    ScreenType screenType = ScreenType.SMALL_MOLECULE;
    //    Multiset<Reagent> reagents = screenResultsDao.findScreenPositiveReagentsNotDistinct(screenType);
    //    assertFalse("no SM positives found!", reagents.isEmpty());
    ScreenPositivesCountStudyCreator creator = new ScreenPositivesCountStudyCreator(null);
    int count = creator.createReagentCountStudy(admin,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_SMALL_MOLECULE_SCREEN_NUMBER,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_SM_STUDY_TITLE,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_SM_STUDY_SUMMARY,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_POSITIVES_ANNOTATION_NAME,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_SM_POSITIVES_ANNOTATION_DESC,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_OVERALL_ANNOTATION_NAME,
                                                     ScreenPositivesCountStudyCreator.DEFAULT_SM_OVERALL_ANNOTATION_DESC,
                                                     screenType,
                                                     genericEntityDao,
                                                     screenResultsDao);
    assertTrue(count > 0);

    //    setComplete();
    //    endTransaction();
    //    startNewTransaction();
    //    genericEntityDao.flush();
    //    genericEntityDao.clear();
    
    // Create a RNAi Study
    
    screenType = ScreenType.RNAI;
    //    reagents = screenResultsDao.findScreenPositiveReagentsNotDistinct(screenType);
    //    assertFalse("no RNAi positives found!", reagents.isEmpty());
    count = creator.createReagentCountStudy(admin,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_RNAI_SCREEN_NUMBER,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_RNAi_STUDY_TITLE,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_RNAi_STUDY_SUMMARY,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_POSITIVES_ANNOTATION_NAME,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_SM_POSITIVES_ANNOTATION_DESC,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_OVERALL_ANNOTATION_NAME,
                                                       ScreenPositivesCountStudyCreator.DEFAULT_SM_OVERALL_ANNOTATION_DESC,
                                                       screenType,
                                                       genericEntityDao,
                                                       screenResultsDao);
    assertTrue(count > 0);

    //    setComplete();
    //    endTransaction();
    //    startNewTransaction();

    setComplete();
    endTransaction();
    startNewTransaction();
    Screen smStudy = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", ScreenPositivesCountStudyCreator.DEFAULT_SMALL_MOLECULE_SCREEN_NUMBER);
    assertNotNull(smStudy);
    Screen rnaiStudy = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", ScreenPositivesCountStudyCreator.DEFAULT_RNAI_SCREEN_NUMBER);
    assertNotNull(rnaiStudy);
    
    smStudy = genericEntityDao.reloadEntity(smStudy);
    Set<Reagent> studyReagents = smStudy.getReagents();
    assertNotNull(studyReagents);
    assertTrue("no reagents in the study:", !studyReagents.isEmpty());
    assertEquals("should be: 1 each for Wells 1,2,3", 3, studyReagents.size());

    log.info("SM Study - display the annotation values");
    for(Reagent reagent:studyReagents)
    {
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
    
    studyReagent = genericEntityDao.reloadEntity(studyReagent, true, Reagent.annotationValues.getPath());
    
    // verify #annotation values: this will be 2 - positives count and overall count
    assertEquals("reagent.getAnnotationValues().size()", 2, studyReagent.getAnnotationValues().size());

    AnnotationValue avPositiveCount = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals(ScreenPositivesCountStudyCreator.DEFAULT_POSITIVES_ANNOTATION_NAME)) {
        avPositiveCount = entry.getValue();
        break;
      }
    }
    assertNotNull(avPositiveCount);
    assertEquals("avPositiveCount is: " + avPositiveCount.getNumericValue(), new Double(crossScreenPositiveCountWell1), avPositiveCount.getNumericValue());

    AnnotationValue avOverallCount = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals(ScreenPositivesCountStudyCreator.DEFAULT_OVERALL_ANNOTATION_NAME)) {
        avOverallCount = entry.getValue();
        break;
      }
    }
    assertNotNull(avOverallCount);
    assertEquals("avOverallCount is: " + avOverallCount.getNumericValue(), new Double(crossScreenCountWell1), avOverallCount.getNumericValue());

    //verify that the SM user can see it
    setCurrentUser(smallMoleculeUser);
    assertTrue("SM user should not be restricted from SM Study", entityViewPolicy.visit(smStudy));
    
    setCurrentUser(rnaiUser);
    assertFalse("rnai user should be restricted from SM Study", entityViewPolicy.visit(smStudy));
        
    //setCurrentUser(smallMoleculeRnaiUser);
    //assertTrue("smallMoleculeRnaiUser user should not be restricted from SM Study", entityViewPolicy.visit(study));
    
    ////////////
    //RNAi Study
    ////////////
    
    rnaiStudy = genericEntityDao.reloadEntity(rnaiStudy);
    studyReagents = rnaiStudy.getReagents();
    assertNotNull(studyReagents);
    assertTrue("no reagents in the RNAi study:", !studyReagents.isEmpty());
    assertEquals("RNAi Study: should be: 1 reagent for each 3 wells", 3, studyReagents.size());

    log.info("RNAi Study");
    for(Reagent reagent:studyReagents)
    {
      log.info("reagent: " + reagent.getVendorId());
      for (Map.Entry<AnnotationType,AnnotationValue> entry : reagent.getAnnotationValues().entrySet()) {
        log.info("Annotation: " + entry.getKey() + ", " + entry.getKey().getName() + ", Study: " +
          ((Screen) entry.getKey().getStudy()).getTitle() + ", " +
          entry.getValue().getNumericValue());
      }
    }
    
    studyReagent = assayWellRnai1.getLibraryWell().getReagents().entrySet().iterator().next().getValue();
    assertTrue(studyReagents.contains(studyReagent));
    
    studyReagent = genericEntityDao.reloadEntity(studyReagent, true, Reagent.annotationValues.getPath());
    
    assertEquals("reagent.getAnnotationValues().size()", 2, studyReagent.getAnnotationValues().size());

    AnnotationValue avPositiveCountRnai = null;
    for (Entry<AnnotationType,AnnotationValue> entry : studyReagent.getAnnotationValues().entrySet()) {
      if (entry.getKey().getName().equals(ScreenPositivesCountStudyCreator.DEFAULT_POSITIVES_ANNOTATION_NAME)) {
        avPositiveCountRnai = entry.getValue();
        break;
      }
    }
    //avPositiveCount = studyReagent.getAnnotationValues().entrySet().iterator().next().getValue();
    assertEquals("av.getNumericValue() is: " + avPositiveCountRnai.getNumericValue(), avPositiveCountRnai.getNumericValue(), new Double(crossScreenPositiveCountWell1Rnai));
  
  }
  
  
  
  public void testFindMutualPositiveColumns()
  {
    //    To test, create 4 screens:
    //      1. "my" screen
    //      2. "others" screen w/no overlapping wells, but with some positives anyway
    //      3. "others" screen w/overlapping wells, but no overlapping positives
    //      4. "others" screen w/overlapping wells, with some overlapping positives
    // the test should assert that the query only returns screen 4 
    // create My screen
    Library library = MakeDummyEntities.makeDummyLibrary(1,ScreenType.SMALL_MOLECULE,1);
    Iterator<Well> wellsIter = library.getWells()
                                      .iterator();
    Well overLapWell1 = wellsIter.next();
    Well overLapWell2 = wellsIter.next();
    Well overLapWell3 = wellsIter.next();
    Well nonOverlapWell1 = wellsIter.next();
    Well nonOverlapWell2 = wellsIter.next();

    Screen myScreen = MakeDummyEntities.makeDummyScreen(0,
                                                        ScreenType.SMALL_MOLECULE);
    myScreen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    ScreenResult screenResult = myScreen.createScreenResult();
    DataColumn col = screenResult.createDataColumn("col1")
                                 .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    AssayWell assayWell = screenResult.createAssayWell(overLapWell1);
    ResultValue resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithNoOverlaps = MakeDummyEntities.makeDummyScreen(1,
                                                                    ScreenType.SMALL_MOLECULE);
    screenWithNoOverlaps.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithNoOverlaps.createScreenResult();
    col = screenResult.createDataColumn("col2")
                      .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(nonOverlapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();

    Screen screenWithOverlapNegative = MakeDummyEntities.makeDummyScreen(2,
                                                                         ScreenType.SMALL_MOLECULE);
    screenWithOverlapNegative.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapNegative.createScreenResult();
    col = screenResult.createDataColumn("col1")
                      .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithOverlapNegative1 = MakeDummyEntities.makeDummyScreen(21,
                                                                          ScreenType.SMALL_MOLECULE);
    screenWithOverlapNegative1.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapNegative1.createScreenResult();
    // make positive that doesn't overlap
    col = screenResult.createDataColumn("col1")
                      .forReplicate(1);
    DataColumn positiveNonOverlapColumn = col;
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithOverlapPositive = MakeDummyEntities.makeDummyScreen(3,
                                                                         ScreenType.SMALL_MOLECULE);
    screenWithOverlapPositive.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapPositive.createScreenResult();
    // make a mutual positive column
    DataColumn mutualColumn = screenResult.createDataColumn("col1").forReplicate(1);
    col = mutualColumn;
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false); // this is the
                                                            // mutual positive
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();


    genericEntityDao.persistEntity(library);
    genericEntityDao.persistEntity(myScreen);
    genericEntityDao.persistEntity(screenWithNoOverlaps);
    genericEntityDao.persistEntity(screenWithOverlapNegative);
    genericEntityDao.persistEntity(screenWithOverlapNegative1);
    genericEntityDao.persistEntity(screenWithOverlapPositive);
    genericEntityDao.flush();

    setComplete();
    endTransaction();

    startNewTransaction();

    List<DataColumn> columns = screenResultsDao.findMutualPositiveColumns(myScreen.getScreenResult());
    for (DataColumn dc : columns) {
      log.info("return dataColumn: " +
               WellSearchResults.makeColumnName(dc, dc.getScreenResult()
                                                      .getScreen()
                                                      .getScreenNumber()));
    }
    assertEquals("should only find one mutual column", 1, columns.size());
    assertTrue("should contain the mutual column: " + mutualColumn,
               columns.contains(mutualColumn));
    assertFalse("should not contain the positiveNonOverlapColumn column: " +
                  positiveNonOverlapColumn,
                columns.contains(positiveNonOverlapColumn));
          
    
  }

  public void testDerivedScreenResults()
  {
    int replicates = 3;
    SortedSet<DataColumn> derivedColSet1 = new TreeSet<DataColumn>();
    SortedSet<DataColumn> derivedColSet2 = new TreeSet<DataColumn>();

    ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();

    for (int i = 0; i < replicates; i++) {
      DataColumn col = screenResult.createDataColumn("col" + i).forReplicate(1).forPhenotype("human");
      derivedColSet1.add(col);
      if (i % 2 == 0) {
        derivedColSet2.add(col);
      }
    }
    DataColumn derivedCol1 = screenResult.createDataColumn("derivedCol1").forReplicate(1).forPhenotype("human");
    for (DataColumn dataColumn : derivedColSet1) {
      derivedCol1.addTypeDerivedFrom(dataColumn);
    }

    DataColumn derivedCol2 = screenResult.createDataColumn("derivedCol2").forReplicate(1).forPhenotype("human");
    for (DataColumn dataColumn : derivedColSet2) {
      derivedCol2.addTypeDerivedFrom(dataColumn);
    }

    genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
    genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());

    setComplete();
    endTransaction();
    startNewTransaction();
          
    List<ScreenResult> screenResults = genericEntityDao.findAllEntitiesOfType(ScreenResult.class);
    screenResult = screenResults.get(0);
    SortedSet<DataColumn> dataColumns =
      new TreeSet<DataColumn>(screenResult.getDataColumns());

    DataColumn derivedCol = dataColumns.last();
    Set<DataColumn> derivedFromSet = derivedCol.getTypesDerivedFrom();
    assertEquals(derivedColSet2, derivedFromSet);

    dataColumns.remove(derivedCol);
    derivedCol = dataColumns.last();
    derivedFromSet = derivedCol.getTypesDerivedFrom();
    assertEquals(derivedColSet1, derivedFromSet);
  }

  // TODO: this unit test has been superceded by ScreenResultLoadedAndDeleterTest; should verify that all assertions tested here are duplicated there, and then remove this test
  public void testDeleteScreenResult()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.saveOrUpdateEntity(library);
    Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
    MakeDummyEntities.makeDummyScreenResult(screen1, library);
    screen1.setLibraryPlatesDataLoadedCount(1);
    screen1.setMaxDataLoadedReplicateCount(2);
    screen1.setMinDataLoadedReplicateCount(2);
    genericEntityDao.saveOrUpdateEntity(screen1);
    setComplete();
    endTransaction();

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
    screen1.invalidate();
    screen1.update();
    setComplete();
    endTransaction();

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
    assertNotNull(screen1.getScreenResult());
    endTransaction();

    screenResultsDao.deleteScreenResult(screen1.getScreenResult());

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1, true);
    assertNull(screen1.getScreenResult());
    assertEquals(0, screen1.getLibraryPlatesDataLoadedCount());
    assertNull(screen1.getMaxDataLoadedReplicateCount());
    assertNull(screen1.getMinDataLoadedReplicateCount());
  }

  /**
   * A ScreenResult's plateNumbers, wells, experimentWellCount, and positives
   * properties should be updated when a ResultValue is added to a
   * ScreenResult's DataColumn.
   */
  public void testScreenResultDerivedPersistentValues()
  {
    SortedSet<AssayWell> expectedAssayWells = Sets.newTreeSet();
    int expectedExperimentalWellCount = 0;
    int expectedPositives = 0;

    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col1 = screenResult.createDataColumn("DataColumn1");
    col1.makePartitionPositiveIndicator();
    DataColumn col2 = screenResult.createDataColumn("DataColumn2");
    col2.makeBooleanPositiveIndicator();
    Library library = new Library((AdministratorUser) screen.getCreatedBy(),
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    for (int i = 1; i <= 10; ++i) {
      int plateNumber = i;
      Well well = library.createWell(new WellKey(plateNumber, "A01"), LibraryWellType.EXPERIMENTAL);
      AssayWell assayWell = screenResult.createAssayWell(well);
      expectedAssayWells.add(assayWell);
      boolean exclude = i % 8 == 0;
      PartitionedValue col1Value = PartitionedValue.values()[i % 4];
      col1.createPartitionedPositiveResultValue(assayWell, col1Value, exclude);
      col2.createBooleanPositiveResultValue(assayWell, false, false);
      if (well.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
        expectedExperimentalWellCount++;
        if (!exclude && col1Value != PartitionedValue.NOT_POSITIVE) {
          log.debug("result value " + col1Value + " is deemed a positive by this test");
          ++expectedPositives;
        }
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);

    setComplete();
    endTransaction();
    startNewTransaction();

    screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
    assertEquals("wells", expectedAssayWells, screen.getScreenResult().getAssayWells());
    assertEquals("experimental well count", expectedExperimentalWellCount, screen.getScreenResult().getExperimentalWellCount().intValue());
    assertEquals("positives", expectedPositives, screen.getScreenResult().getDataColumnsList().get(0).getPositivesCount().intValue());
    assertEquals("0 positives (but not null)", 0, screen.getScreenResult().getDataColumnsList().get(1).getPositivesCount().intValue());
  }


  public void testFindResultValuesByPlate()
  {
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col1 = screenResult.createDataColumn("Raw Value").makeNumeric(3);
    DataColumn col2 = screenResult.createDataColumn("Derived Value").makeNumeric(3);
    Library library = new Library((AdministratorUser) screen.getCreatedBy(),
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    for (int iPlate = 1; iPlate <= 3; ++iPlate) {
      int plateNumber = iPlate;
      for (int iWell = 0; iWell < 10; ++iWell) {
        Well well = library.createWell(new WellKey(plateNumber, "A" + (iWell + 1)), LibraryWellType.EXPERIMENTAL);
        AssayWell assayWell = screenResult.createAssayWell(well);
        col1.createResultValue(assayWell, (double) iWell);
        col2.createResultValue(assayWell, iWell + 10.0);
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);
    setComplete();
    endTransaction();

    startNewTransaction();
    // test findResultValuesByPlate(Integer, DataColumn)
    Map<WellKey,ResultValue> resultValues1 = screenResultsDao.findResultValuesByPlate(2, col1);
    assertEquals("result values size", 10, resultValues1.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues1.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", new Double(iWell), rv.getNumericValue());
    }
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
  
  private void setCurrentUser(ScreensaverUser user)
  {
    entityViewPolicy = new IccblEntityViewPolicy(user, genericEntityDao);
  }
}
