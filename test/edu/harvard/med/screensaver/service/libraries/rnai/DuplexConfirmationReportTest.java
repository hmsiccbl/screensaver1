// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries.rnai;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.service.libraries.rnai.DuplexConfirmationReport.ConfirmationReport;

public class DuplexConfirmationReportTest extends AbstractTransactionalSpringContextTests
{
  private static final Logger log = Logger.getLogger(DuplexConfirmationReportTest.class);

  protected GenericEntityDAO genericEntityDao;
  protected DuplexConfirmationReport duplexConfirmationReport;
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  protected SchemaUtil schemaUtil;
  protected TestDataFactory dataFactory = new TestDataFactory();

  private SilencingReagent _poolReagent;
  private List<SilencingReagent> _duplexReagents = Lists.newArrayList();
  private List<Well> _duplexWells = Lists.newArrayList();
  private Screen _screenRnai1;
  private Screen _screenRnai2;
  private Screen _screenRnai3;
  private Screen _screenRnai4;

  private AdministratorUser admin = null;
  private List<String> sequences = Lists.newArrayList("GAUGAACAGACUCCAAUUC", "GAUGAAGAGCCUAUUGAAG", "GAGCUUACAACCUGCCUUA", "GAACAGACUCCAAUUCAUA");

  private Screen _screenRnaiPool;

  private Well _poolWell;
  private Well _poolWell1;


  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public DuplexConfirmationReportTest()
  {
    setPopulateProtectedVariables(true);
  }

  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }

  private void setupInTransaction()
  {
    setupInTransaction_createReagents();
    setupInTransaction_createScreenResults();
  }


  private void setupInTransaction_createReagents()
  {
    final AdministratorUser releaseAdmin = dataFactory.newInstance(AdministratorUser.class);
    releaseAdmin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.persistEntity(releaseAdmin);

    Library duplexLibrary = dataFactory.newInstance(Library.class);
    duplexLibrary.setLibraryName("duplexLibrary");
    genericEntityDao.persistEntity(duplexLibrary);

    Library duplexLibrary1 = dataFactory.newInstance(Library.class);
    duplexLibrary1.setLibraryName("duplexLibrary1");
    genericEntityDao.persistEntity(duplexLibrary1);

    setComplete();
    endTransaction();
    startNewTransaction();

    //// Create the confirming wells
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary, releaseAdmin, "");
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary1, releaseAdmin, "");

    duplexLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary", false, Library.wells.getPath(), Library.contentsVersions.getPath());
    duplexLibrary.setStartPlate(1);
    duplexLibrary.setEndPlate(1);
    duplexLibrary.setScreenType(ScreenType.RNAI);
    
    for (int i = 0; i < sequences.size(); i++) {
      Well well = duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A0" + (i + 1)), LibraryWellType.EXPERIMENTAL);
      well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1." + i), SilencingReagentType.SIRNA, sequences.get(i));
      _duplexWells.add(well);
    }

    genericEntityDao.saveOrUpdateEntity(duplexLibrary);

    //// create the non-confirming wells
    duplexLibrary1 = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary1", false, Library.wells.getPath(), Library.contentsVersions.getPath());
    duplexLibrary1.setStartPlate(10);
    duplexLibrary1.setEndPlate(10);
    duplexLibrary1.setScreenType(ScreenType.RNAI);

    for (int i = 0; i < sequences.size(); i++) {
      Well well = duplexLibrary1.createWell(new WellKey(duplexLibrary1.getStartPlate(), "A0" + (i + 1)), LibraryWellType.EXPERIMENTAL);
      well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1." + i), SilencingReagentType.SIRNA, sequences.get(i) +
        "X");
      _duplexWells.add(well);
    }

    genericEntityDao.saveOrUpdateEntity(duplexLibrary1);
    ////

    setComplete();
    endTransaction();
    startNewTransaction();

    libraryContentsVersionManager.releaseLibraryContentsVersion(duplexLibrary.getLatestContentsVersion(), releaseAdmin);

    setComplete();
    endTransaction();
    startNewTransaction();

    List<Well> duplexWells = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary);
    List<Well> duplexWells1 = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary1);

    Library poolLibrary = dataFactory.newInstance(Library.class);
        poolLibrary.setLibraryName("Pool");
    poolLibrary.setPool(true);

    dataFactory.newInstance(LibraryContentsVersion.class, poolLibrary);
    poolLibrary.setStartPlate(2);
    poolLibrary.setEndPlate(2);
    poolLibrary.setScreenType(ScreenType.RNAI);
    _poolWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL);
    String poolSequence = sequences.get(0);
    for (int k = 1; k < sequences.size(); k++)
      poolSequence += "," + sequences.get(k);
    
    _poolReagent = _poolWell.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1"), SilencingReagentType.SIRNA, poolSequence);
    _poolReagent.withDuplexWell(duplexWells.get(0));
    _poolReagent.withDuplexWell(duplexWells.get(1));
    _poolReagent.withDuplexWell(duplexWells.get(2));
    _poolReagent.withDuplexWell(duplexWells.get(3));

    // create an unconfirmed poolwell
    _poolWell1 = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL);

    SilencingReagent pr = _poolWell1.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1a"), SilencingReagentType.SIRNA, poolSequence);
    pr.withDuplexWell(duplexWells1.get(0));
    pr.withDuplexWell(duplexWells1.get(1));
    pr.withDuplexWell(duplexWells1.get(2));
    pr.withDuplexWell(duplexWells1.get(3));

    genericEntityDao.persistEntity(poolLibrary);

    setComplete();
    endTransaction();
    startNewTransaction();

    libraryContentsVersionManager.releaseLibraryContentsVersion(poolLibrary.getLatestContentsVersion(), releaseAdmin);

    setComplete();
    endTransaction();
    startNewTransaction();

    poolLibrary =
        genericEntityDao.findEntityByProperty(Library.class,
                                              "libraryName",
                                              "Pool",
                                              true,
                                              Library.wells.to(Well.latestReleasedReagent).to(SilencingReagent.duplexWells).to(Well.latestReleasedReagent).getPath());

    assertEquals(duplexLibrary.getWells(),
                 ((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexWells());
    assertEquals(Sets.newHashSet(sequences),
                 Sets.newHashSet(Iterables.transform(((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexSilencingReagents(),
                                                     new Function<SilencingReagent,String>() {
                                                       public String apply(SilencingReagent sr)
                                                       {
                                                         return sr.getSequence();
                                                       }
                                                     })));
    for (String sequence : sequences) {
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
    _screenRnai1 = MakeDummyEntities.makeDummyScreen(10, ScreenType.RNAI); // set to 10 so that sort may be tested
    screenResult = _screenRnai1.createScreenResult();

    // create confirmed positive results
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    AssayWell assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    Reagent reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1,
                                                                     ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                     false);
    assertTrue(resultValue.isPositive());

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(2));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1,
                                                                     ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                     false);
    assertTrue(resultValue.isPositive());

    // create a boolean positive indicator that will not be counted
    _screenRnai2 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai2.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeBooleanPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(0));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createBooleanPositiveResultValue(assayWellRnai1, true, false);
    assertTrue(resultValue.isPositive());

    // create a confirmed negative
    _screenRnai3 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai3.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(1));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.FALSE_POSITIVE, false);
    assertFalse(resultValue.isPositive());

    // create another confirmed positive
    _screenRnai4 = MakeDummyEntities.makeDummyScreen(screenNumber++, ScreenType.RNAI);
    screenResult = _screenRnai4.createScreenResult();
    col = screenResult.createDataColumn("col1").forReplicate(1);
    col.makeConfirmedPositiveIndicator();

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(2));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.INCONCLUSIVE, false);
    assertFalse(resultValue.isPositive());

    assayWellRnai1 = screenResult.createAssayWell(_duplexWells.get(3));
    reagentRnai1 = assayWellRnai1.getLibraryWell().getLatestReleasedReagent();
    resultValue = col.createConfirmedPositiveResultValue(assayWellRnai1, ConfirmedPositiveValue.CONFIRMED_POSITIVE, false);
    assertTrue(resultValue.isPositive());

    setComplete();
    endTransaction();
    startNewTransaction();

    genericEntityDao.saveOrUpdateEntity(_screenRnaiPool);
    genericEntityDao.saveOrUpdateEntity(_screenRnai1);
    genericEntityDao.saveOrUpdateEntity(_screenRnai2);
    genericEntityDao.saveOrUpdateEntity(_screenRnai3);
    genericEntityDao.saveOrUpdateEntity(_screenRnai4);
    //    // create a "negative"
    //    assayWellRnai1a = screenResult.createAssayWell(wellRnai3);
    //    reagentRnai1a = assayWellRnai1a.getLibraryWell().getLatestReleasedReagent();
    //    resultValue = col.createBooleanPositiveResultValue(assayWellRnai1a, false, false);
    //    assertTrue(!resultValue.isPositive());

  }

  public void testDuplexReconfirmationReport()
  {
    setupInTransaction();
    setComplete();
    endTransaction();
    startNewTransaction();

    _screenRnai1 = genericEntityDao.reloadEntity(_screenRnai1);
    _screenRnai2 = genericEntityDao.reloadEntity(_screenRnai2);
    _screenRnai3 = genericEntityDao.reloadEntity(_screenRnai3);
    _screenRnai4 = genericEntityDao.reloadEntity(_screenRnai4);

    // TODO: one way to test may be to create a ReconfirmationReport to compare this result to (using equals)
    ConfirmationReport report = duplexConfirmationReport.getDuplexReconfirmationReport(_poolReagent);
    
    assertTrue(report.reagents.size() == 4);
    assertEquals(Sets.newHashSet(sequences),
                 Sets.newHashSet(Iterables.transform(report.reagents,
                                                     new Function<SilencingReagent,String>() {
                                                       public String apply(SilencingReagent sr)
                                                       {
                                                         return sr.getSequence();
                                                       }
                                                     })));

    assertEquals(Lists.newArrayList(_screenRnai3, _screenRnai4, _screenRnai1), report.screens); // Note: ordering is because of the screen_number sort
    assertTrue(report.results.containsKey(_screenRnai1));
    assertFalse(report.results.containsKey(_screenRnai2));
    assertTrue(report.results.containsKey(_screenRnai3));
    assertTrue(report.results.containsKey(_screenRnai4));

    assertTrue(report.results.get(_screenRnai1).containsKey(_duplexReagents.get(0)));
    assertTrue(report.results.get(_screenRnai1).containsKey(_duplexReagents.get(2)));
    assertTrue(report.results.get(_screenRnai3).containsKey(_duplexReagents.get(1)));
    assertTrue(report.results.get(_screenRnai4).containsKey(_duplexReagents.get(3)));
    assertEquals(report.results.get(_screenRnai1).get(_duplexReagents.get(0)), ConfirmedPositiveValue.CONFIRMED_POSITIVE);
    assertEquals(report.results.get(_screenRnai3).get(_duplexReagents.get(1)), ConfirmedPositiveValue.FALSE_POSITIVE);
    assertEquals(report.results.get(_screenRnai4).get(_duplexReagents.get(2)), ConfirmedPositiveValue.INCONCLUSIVE);
    assertEquals(report.results.get(_screenRnai4).get(_duplexReagents.get(3)), ConfirmedPositiveValue.CONFIRMED_POSITIVE);

  }

}
