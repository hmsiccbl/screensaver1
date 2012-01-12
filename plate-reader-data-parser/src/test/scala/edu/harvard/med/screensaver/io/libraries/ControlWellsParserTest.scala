package edu.harvard.med.screensaver.io.libraries

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

import edu.harvard.med.screensaver.model.libraries.PlateSize
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType

class ControlWellsParserTest extends AssertionsForJUnit {

  val parser = new ControlWellsParser(PlateSize.WELLS_96, AssayWellControlType.ASSAY_CONTROL)

  def newControlWell(wellName: String) = ControlWell(new WellName(wellName), None, AssayWellControlType.ASSAY_CONTROL)
  def newControlWell(wellName: String, label: String) = ControlWell(new WellName(wellName), Some(label), AssayWellControlType.ASSAY_CONTROL)

  @Test def testEmpty() = {
    expect(Seq[ControlWell]()) { parser.parse("").get }
    expect(Seq[ControlWell]()) { parser.parse("   ").get }
  }

  @Test def testSingleWellName = expect(Seq(newControlWell("A01"))) { parser.parse("A01").get }
  @Test def testSingleWellNameLowercase = expect(Seq(newControlWell("A01"))) { parser.parse("a01").get }
  @Test def testMultipleWellNames = expect(Seq(newControlWell("A01"), newControlWell("B02"))) { parser.parse("A01 B02").get }
  @Test def testFullRow = expect(Seq(newControlWell("C01"), newControlWell("C02"), newControlWell("C03"), newControlWell("C04"), newControlWell("C05"), newControlWell("C06"), newControlWell("C07"), newControlWell("C08"), newControlWell("C09"), newControlWell("C10"), newControlWell("C11"), newControlWell("C12"))) { parser.parse("C").get }
  @Test def testFullColumn = {
    expect(Seq(newControlWell("A01"), newControlWell("B01"), newControlWell("C01"), newControlWell("D01"), newControlWell("E01"), newControlWell("F01"), newControlWell("G01"), newControlWell("H01"))) { parser.parse("1").get }
    expect(parser.parse("1").get) { parser.parse("01").get }
  }
  @Test def testRectangle = expect(Seq(newControlWell("A01"), newControlWell("A02"), newControlWell("B01"), newControlWell("B02"))) { parser.parse("A01..B02").get }

  @Test def testControlType {
    expect(Seq(ControlWell(new WellName("A01"), None, AssayWellControlType.ASSAY_CONTROL))) {
      new ControlWellsParser(PlateSize.WELLS_96, AssayWellControlType.ASSAY_CONTROL).parse("A01").get
    }
    expect(Seq(ControlWell(new WellName("A01"), None, AssayWellControlType.ASSAY_POSITIVE_CONTROL))) {
      new ControlWellsParser(PlateSize.WELLS_96, AssayWellControlType.ASSAY_POSITIVE_CONTROL).parse("A01").get
    }
    expect(Seq(ControlWell(new WellName("A01"), None, AssayWellControlType.OTHER_CONTROL))) {
      new ControlWellsParser(PlateSize.WELLS_96, AssayWellControlType.OTHER_CONTROL).parse("A01").get
    }
  }

  @Test def testControlLabels {
    expect(Seq(newControlWell("A01", "plc1"),
      newControlWell("B01", "plc2"),
      newControlWell("B02", "plc2"))) { parser.parse("A01=plc1 B01-B02=plc2").get }
    expect(Seq(newControlWell("A01", "plc1"),
      newControlWell("A05", "plc2"),
      newControlWell("B05", "plc2"),
      newControlWell("C05", "plc2"),
      newControlWell("D05", "plc2"),
      newControlWell("E05", "plc2"),
      newControlWell("F05", "plc2"),
      newControlWell("G05", "plc2"),
      newControlWell("H05", "plc2"))) { parser.parse("A01=plc1 5=plc2").get }
    expect(Seq(newControlWell("A01", "two words"))) { parser.parse("A01=\"two words\"").get }
    expect(Seq(newControlWell("A01", "pun<tu@t!on is okay! #2 x-y-z"))) { parser.parse("A01=\"pun<tu@t!on is okay! #2 x-y-z\"").get }
  }

  @Test def testQuadrantControlLabels {
    expect(Seq(newControlWell("A01", "q1|q2|q3|q4"),
      newControlWell("B01", "label"))) { val r = parser.parse("A01=q1|q2|q3|q4 B01=label"); if (r successful) r.get else throw new RuntimeException(r.toString) }
  }

  @Test
  def parseError() = {
    //    println(Parser.parse("5-g"))
    //    println(Parser.parse("5 x 9"))
    //    println(Parser.parse("1--10"))
    //    println(Parser.parse("10.9"))
  }

}