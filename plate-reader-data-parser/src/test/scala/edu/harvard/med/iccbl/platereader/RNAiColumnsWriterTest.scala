package edu.harvard.med.iccbl.platereader

import java.io.File
import org.joda.time.LocalDate
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import edu.harvard.med.iccbl.platereader.FindSilencingReagent
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType
import edu.harvard.med.screensaver.model.libraries.SilencingReagent
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.screens.ScreenType
import edu.harvard.med.screensaver.test.MakeDummyEntities
import jxl.BooleanCell
import jxl.NumberCell
import jxl.Workbook
import edu.harvard.med.screensaver.model.users.AdministratorUser
import jxl.write.Label

class RNAiColumnsWriterTest extends AssertionsForJUnit {

  val lib = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1)
  val well = lib.getWells().first()

  private def doTestForValue(finder: FindSilencingReagent, expected: Any)(actual: Workbook => Any) {
    val writer = new RNAiColumnsWriter(finder);
    val file = File.createTempFile("rnaiColumnsWriterTest", ".xls")
    file.deleteOnExit()
    val wbk = Workbook.createWorkbook(file)
    val sheet = wbk.createSheet("sheet", 0)
    val wellKey = new WellKey(1000, "A01")
    for (h <- writer.headers.zipWithIndex) sheet.addCell(new Label(h._2, 0, h._1)) // ensure that all columns we'll inspect are defined in the worksheet
    sheet.addCell(new Label(0, 1, wellKey.toString)) // ensure that data row we'll inpsect is defined in the worksheet
    writer.writeValues(sheet, wellKey, 1, 1)
    wbk.write();
    wbk.close();

    expect(expected) { actual(Workbook.getWorkbook(file)) }
  }

  @Test
  def testExtantValue() {
    val silencingReagentFinder = new FindSilencingReagent() {
      def apply(wk: WellKey) = {
        val sr = well.getLatestReleasedReagent().asInstanceOf[SilencingReagent]
        sr.getFacilityGene.withEntrezgeneSymbol("sym")
        sr.getFacilityGene.withEntrezgeneId(1)
        sr.getWell.getLibrary.setPool(true)
        sr.getWell.setDeprecationActivity(new AdministrativeActivity(new AdministratorUser("", ""), new LocalDate(), AdministrativeActivityType.WELL_DEPRECATION))
        sr
      }
    }
    val wellKey = well.getWellKey
    doTestForValue(silencingReagentFinder, "sym") { _.getSheet(0).getCell(1, 1).getContents() }
    doTestForValue(silencingReagentFinder, 1.0) { _.getSheet(0).getCell(2, 1).asInstanceOf[NumberCell].getValue }
    doTestForValue(silencingReagentFinder, "GB" + wellKey.hashCode()) { _.getSheet(0).getCell(3, 1).getContents() }
    doTestForValue(silencingReagentFinder, "rnai0") { _.getSheet(0).getCell(4, 1).getContents() }
    doTestForValue(silencingReagentFinder, "geneName" + wellKey) { _.getSheet(0).getCell(5, 1).getContents() }
    doTestForValue(silencingReagentFinder, true) { _.getSheet(0).getCell(6, 1).asInstanceOf[BooleanCell].getValue }
  }

  @Test
  def testEmtpyEntrezgeneSymbols() {
    val silencingReagentFinder = new FindSilencingReagent() {
      def apply(wk: WellKey) = {
        val sr = well.getLatestReleasedReagent().asInstanceOf[SilencingReagent]
        // not adding an entrezgene symbol
        sr
      }
    }
    doTestForValue(silencingReagentFinder, "") { _.getSheet(0).getCell(1, 1).getContents() }
  }

  // TODO: correct this test, by having no released reagent in well
  @Test
  def testNoReleasedReagent() {
    val silencingReagentFinder = new FindSilencingReagent() {
      def apply(wk: WellKey) = {
        val sr = well.getLatestReleasedReagent().asInstanceOf[SilencingReagent]
        // not adding an entrezgene symbol
        sr
      }
    }
    doTestForValue(silencingReagentFinder, "") { _.getSheet(0).getCell(1, 1).getContents() }
  }

  @Test
  def testNoSuchWell() {
    val silencingReagentFinder = new FindSilencingReagent() { def apply(wk: WellKey) = null }
    doTestForValue(silencingReagentFinder, "") { _.getSheet(0).getCell(1, 1).getContents() }
  }
}

