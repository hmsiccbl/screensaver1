package edu.harvard.med.screensaver.io.libraries
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test

class PlateNumbersParserTest extends AssertionsForJUnit {

  @Test
  def testIt() = {
    expect(Seq[Int]()) { PlateNumbersParser.parse("").get }
    expect(Seq[Int]()) { PlateNumbersParser.parse("   ").get }
    expect(Seq(1)) { PlateNumbersParser.parse("1").get }
    expect(Seq(1)) { PlateNumbersParser.parse(" 1 ").get }
    expect(Seq(5, 6, 8, 10, 11, 12)) { PlateNumbersParser.parse("5, 6 8\n10-12").get }
    expect(Seq(6, 5, 4, 2, 1)) { PlateNumbersParser.parse("6-4 2, 1").get }
  }

  @Test
  def parseError() = {
    println(PlateNumbersParser.parse("5-g"))
    println(PlateNumbersParser.parse("5 x 9"))
    println(PlateNumbersParser.parse("1--10"))
    println(PlateNumbersParser.parse("10.9"))
  }

}