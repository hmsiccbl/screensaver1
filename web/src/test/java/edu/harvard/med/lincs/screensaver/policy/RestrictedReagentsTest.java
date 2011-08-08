
package edu.harvard.med.lincs.screensaver.policy;

import java.io.StringWriter;
import java.io.Writer;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.lincs.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.io.libraries.WellSdfWriter;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.TestDataFactory.PostCreateHook;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;

/** Integration tests to verify that restricted reagents' properties are in fact restricted throughout the application */
@ContextConfiguration(locations = { "/spring-context-test-ui-lincs.xml" }, inheritLocations = false)
@Transactional
public class RestrictedReagentsTest extends AbstractSpringPersistenceTest
{
  @Autowired
  private CurrentScreensaverUser currentScreensaverUser;
  @Autowired
  private WellSearchResults wellsBrowser;
  @Autowired
  private WellViewer wellViewer;

  private SmallMoleculeReagent _restrictedSmr;
  private SmallMoleculeReagent _unrestrictedSmr;
  private SilencingReagent _restrictedSr;
  private SilencingReagent _unrestrictedSr;

  private ScreeningRoomUser guestUser;
  private AdministratorUser adminUser;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    dataFactory.addPostCreateHook(SmallMoleculeReagent.class, new PostCreateHook<SmallMoleculeReagent>() {
      @Override
      public void postCreate(String callStack, SmallMoleculeReagent smr)
      {
        if (callStack.endsWith("|restricted")) {
          smr.forRestrictedStructure();
          smr.getWell().setFacilityId("restrictedSmr");
        }
        else {
          smr.getWell().setFacilityId("unrestrictedSmr");
        }
        AdministrativeActivity releaseActivity =
          new AdministrativeActivity(dataFactory.newInstance(AdministratorUser.class),
                                     new LocalDate(),
                                     AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE);
        smr.getWell().getLibrary().getLatestContentsVersion().release(releaseActivity);
      }
    });
    dataFactory.addPostCreateHook(SilencingReagent.class, new PostCreateHook<SilencingReagent>() {
      @Override
      public void postCreate(String callStack, SilencingReagent sr)
      {
        if (callStack.endsWith("restricted")) {
          sr.withRestrictedSequence(true);
          sr.getWell().setFacilityId("restrictedSr");
        }
        else {
          sr.getWell().setFacilityId("unrestrictedSr");
        }
        AdministrativeActivity releaseActivity =
          new AdministrativeActivity(dataFactory.newInstance(AdministratorUser.class),
                                     new LocalDate(),
                                     AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE);
        sr.getWell().getLibrary().getLatestContentsVersion().release(releaseActivity);
      }
    });
    _restrictedSmr = dataFactory.newInstance(SmallMoleculeReagent.class, "restricted");
    _unrestrictedSmr = dataFactory.newInstance(SmallMoleculeReagent.class, "unrestricted");
    _restrictedSr = dataFactory.newInstance(SilencingReagent.class, "restricted");
    _unrestrictedSr = dataFactory.newInstance(SilencingReagent.class, "unrestricted");

    guestUser = new ScreeningRoomUser("Guest", "User", ScreeningRoomUserClassification.OTHER);
    guestUser.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    guestUser.addScreensaverUserRole(ScreensaverUserRole.GUEST);
    adminUser = new AdministratorUser("Admin", "User");
    adminUser.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    adminUser.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
    guestUser = genericEntityDao.mergeEntity(guestUser);
    adminUser = genericEntityDao.mergeEntity(adminUser);

    assertNotNull(_restrictedSmr.getSmiles());

    genericEntityDao.flush();
  }

  public void testRestrictedProperties()
  {
    currentScreensaverUser.setScreensaverUser(guestUser);

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getSmiles());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getSmiles());

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getInchi());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getInchi());

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getMolfile());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getMolfile());

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getMolecularFormula());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getMolecularFormula());

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getMolecularWeight());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getMolecularWeight());

    assertNull(((SmallMoleculeReagent) _restrictedSmr.restrict()).getMolecularMass());
    assertNotNull(((SmallMoleculeReagent) _unrestrictedSmr.restrict()).getMolecularMass());
  }

  public void testWellSearchResults()
  {
    doTestPropertyRestriction(guestUser, "Compound SMILES", _restrictedSmr, null);
    doTestPropertyRestriction(guestUser, "Compound SMILES", _unrestrictedSmr, _unrestrictedSmr.getSmiles());
    doTestPropertyRestriction(adminUser, "Compound SMILES", _restrictedSmr, _restrictedSmr.getSmiles());
    doTestPropertyRestriction(adminUser, "Compound SMILES", _unrestrictedSmr, _unrestrictedSmr.getSmiles());

    doTestPropertyRestriction(guestUser, "Compound InChi", _restrictedSmr, null);
    doTestPropertyRestriction(guestUser, "Compound InChi", _unrestrictedSmr, _unrestrictedSmr.getInchi());
    doTestPropertyRestriction(adminUser, "Compound InChi", _restrictedSmr, _restrictedSmr.getInchi());
    doTestPropertyRestriction(adminUser, "Compound InChi", _unrestrictedSmr, _unrestrictedSmr.getInchi());

    //    doTestPropertyRestriction(guestUser, "Molecular Formula", _restrictedSmr, null);
    //    doTestPropertyRestriction(guestUser, "Molecular Formula", _unrestrictedSmr, _unrestrictedSmr.getMolecularFormula());
    //    doTestPropertyRestriction(adminUser, "Molecular Formula", _restrictedSmr, _restrictedSmr.getMolecularFormula());
    //    doTestPropertyRestriction(adminUser, "Molecular Formula", _unrestrictedSmr, _unrestrictedSmr.getMolecularFormula());
    //
    //    doTestPropertyRestriction(guestUser, "Molecular Weight", _restrictedSmr, null);
    //    doTestPropertyRestriction(guestUser, "Molecular Weight", _unrestrictedSmr, _unrestrictedSmr.getMolecularWeight());
    //    doTestPropertyRestriction(adminUser, "Molecular Weight", _restrictedSmr, _restrictedSmr.getMolecularWeight());
    //    doTestPropertyRestriction(adminUser, "Molecular Weight", _unrestrictedSmr, _unrestrictedSmr.getMolecularWeight());
    //
    //    doTestPropertyRestriction(guestUser, "Molecular Mass", _restrictedSmr, null);
    //    doTestPropertyRestriction(guestUser, "Molecular Mass", _unrestrictedSmr, _unrestrictedSmr.getMolecularMass());
    //    doTestPropertyRestriction(adminUser, "Molecular Mass", _restrictedSmr, _restrictedSmr.getMolecularMass());
    //    doTestPropertyRestriction(adminUser, "Molecular Mass", _unrestrictedSmr, _unrestrictedSmr.getMolecularMass());

  }

  private void doTestPropertyRestriction(ScreensaverUser user,
                                         String columnName,
                                         SmallMoleculeReagent smr,
                                         Object expectedValue)
  {
    currentScreensaverUser.setScreensaverUser(user);
    wellsBrowser.searchAllReagents();
    DataTableModel<Tuple<String>> dataTableModel = wellsBrowser.getDataTableModel();
    TextTupleColumn<Well,String> idColumn = (TextTupleColumn<Well,String>) wellsBrowser.getColumnManager().getColumn("Facility-Salt-Batch-ID");
    TextTupleColumn<Well,String> valueColumn = (TextTupleColumn<Well,String>) wellsBrowser.getColumnManager().getColumn(columnName);
    valueColumn.setVisible(true);
    assertEquals(4, dataTableModel.getRowCount());
    idColumn.getCriterion().setOperatorAndValue(Operator.TEXT_STARTS_WITH, smr.getWell().getFacilityId());
    assertEquals(1, dataTableModel.getRowCount());
    dataTableModel.setRowIndex(0);
    Tuple<String> row = (Tuple<String>) dataTableModel.getRowData();
    Object actualValue = valueColumn.getCellValue(row);
    if (expectedValue == null) {
      assertNull(actualValue);
    }
    else {
      assertEquals(expectedValue, actualValue);
    }
  }

  public void testSDFWriter()
  {
    doTestSdfWriter(guestUser, _restrictedSmr, false);
    doTestSdfWriter(guestUser, _unrestrictedSmr, true);
    doTestSdfWriter(adminUser, _restrictedSmr, true);
    doTestSdfWriter(adminUser, _unrestrictedSmr, true);
  }

  private void doTestSdfWriter(ScreensaverUser user, SmallMoleculeReagent smr, boolean isRestrictedPropertyVisible)
  {
    currentScreensaverUser.setScreensaverUser(user);
    Writer writer = new StringWriter();
    new WellSdfWriter(writer).write(smr.getWell(), smr.getLibraryContentsVersion());
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getSmiles()));
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getMolfile()));
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getInchi()));
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getMolecularFormula().toString()));
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getMolecularMass().toString()));
    assertEquals(isRestrictedPropertyVisible, writer.toString().contains(smr.getMolecularWeight().toString()));
  }

  public void testWellViewer()
  {
    doTestWellViewer(guestUser, _restrictedSmr, false);
    doTestWellViewer(guestUser, _unrestrictedSmr, true);
    doTestWellViewer(adminUser, _restrictedSmr, true);
    doTestWellViewer(adminUser, _unrestrictedSmr, true);
  }

  private void doTestWellViewer(ScreensaverUser user, SmallMoleculeReagent smr, boolean isRestrictedPropertyVisible)
  {
    currentScreensaverUser.setScreensaverUser(user);
    wellViewer.viewEntity(smr.getWell());
    assertNotNull(wellViewer.getEntity());

    SmallMoleculeReagent restrictedReagent = (SmallMoleculeReagent) wellViewer.getRestrictedReagent();
    assertNotNull(wellViewer.getRestrictedReagent());

    if (isRestrictedPropertyVisible) {
      assertEquals(smr.getSmiles(), restrictedReagent.getSmiles());
      assertEquals(smr.getInchi(), restrictedReagent.getInchi());
      assertEquals(smr.getMolfile(), restrictedReagent.getMolfile());
      assertEquals(smr.getMolecularFormula(), restrictedReagent.getMolecularFormula());
      assertEquals(smr.getMolecularMass(), restrictedReagent.getMolecularMass());
      assertEquals(smr.getMolecularWeight(), restrictedReagent.getMolecularWeight());
    } 
    else {
      assertNull(restrictedReagent.getSmiles());
      assertNull(restrictedReagent.getInchi());
      assertNull(restrictedReagent.getMolfile());
      assertNull(restrictedReagent.getMolecularFormula());
      assertNull(restrictedReagent.getMolecularMass());
      assertNull(restrictedReagent.getMolecularWeight());
    }      
  }

  public void testDownloadAttachedFiles()
  {
    fail("not implemented");
  }
}
