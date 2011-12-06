package edu.harvard.med.screensaver.io.libraries

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.libraries.PlateSize

class PlateWellNamesParserTest extends AssertionsForJUnit {

  val parser = new PlateWellNamesParser(PlateSize.WELLS_96)

  @Test def testEmpty() = {
    expect(Seq[WellName]()) { parser.parse("").get }
    expect(Seq[WellName]()) { parser.parse("   ").get }
  }

  @Test def testSingleWellName = expect(Seq(new WellName("A01"))) { parser.parse("A01").get }
  @Test def testSingleWellNameLowercase = expect(Seq(new WellName("A01"))) { parser.parse("a01").get }
  @Test def testMultipleWellNames = expect(Seq(new WellName("A01"), new WellName("B02"))) { parser.parse("A01 B02").get }
  @Test def testFullRow = expect(Seq(new WellName("C01"), new WellName("C02"), new WellName("C03"), new WellName("C04"), new WellName("C05"), new WellName("C06"), new WellName("C07"), new WellName("C08"), new WellName("C09"), new WellName("C10"), new WellName("C11"), new WellName("C12"))) { parser.parse("C").get }
  @Test def testFullColumn = {
    expect(Seq(new WellName("A01"), new WellName("B01"), new WellName("C01"), new WellName("D01"), new WellName("E01"), new WellName("F01"), new WellName("G01"), new WellName("H01"))) { parser.parse("1").get }
    expect(parser.parse("1").get) { parser.parse("01").get }
  }
  @Test def testRectangle = expect(Seq(new WellName("A01"), new WellName("A02"), new WellName("B01"), new WellName("B02"))) { parser.parse("A01..B02").get }

  @Test
  def parseError() = {
    //    println(Parser.parse("5-g"))
    //    println(Parser.parse("5 x 9"))
    //    println(Parser.parse("1--10"))
    //    println(Parser.parse("10.9"))
  }

}