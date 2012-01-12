package edu.harvard.med.iccbl.platereader.parser

import java.io.InputStream
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import scala.util.parsing.input.CharSequenceReader
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.iccbl.platereader.PlateDim

class TabDelimitedRawPlateReaderDataParserTest extends AssertionsForJUnit {
  val plateHeaderInput = "Blah Blah Blah\r\n\r\n#1001 ID ID ID\r\n\r\nResults:\r\n"
  val plate1MatrixInput =
    "\t1\t2\t3\t4\r\n" +
      "A\t1.0\t1.1\t1.2\t1.3\r\n" +
      "B\t2.0\t-2.1\t2.2\t2.3\r\n" +
      "C\t3.0\t3.1\t3.2\t3.33E1\r\n"

  val plate2MatrixInput =
    "\t1\t2\t3\t4\r\n" +
      "A\t1.1\t1.11\t1.21\t1.31\r\n" +
      "B\t2.1\t-2.11\t2.21\t2.31\r\n" +
      "C\t3.1\t3.11\t3.21\t3.31E1\r\n"

  val postscriptInput = "Hum Drum Hum Drum\r\nHum Drum Hum\r\n\r\n"
  val envisionPostscriptInput = "\r\n\r\nBasic assay information\r\nHum Drum Hum Drum\r\nHum Drum Hum\r\n\r\n" +
    "\t1\t2\t3\t4\r\n" +
    "A\t-\t-\t-\t-\r\n" +
    "B\t-\t-\t-\t-\r\n" +
    "C\t-\t-\t-\t-\r\n" +
    "Hum Drum Hum Drum\r\nHum Drum Hum\r\n" +
    "Exported with EnVision Workstation version 1.12\r\n\r\n"
  val plate1RecordInput = plateHeaderInput + plate1MatrixInput
  val plate2RecordInput = plateHeaderInput + plate2MatrixInput
  val plate1DataParsed = new PlateMatrix(List(
    List[BigDecimal](1.0, 1.1, 1.2, 1.3),
    List[BigDecimal](2.0, -2.1, 2.2, 2.3),
    List[BigDecimal](3.0, 3.1, 3.2, 33.3)))
  val plate2DataParsed = new PlateMatrix(List(
    List[BigDecimal](1.1, 1.11, 1.21, 1.31),
    List[BigDecimal](2.1, -2.11, 2.21, 2.31),
    List[BigDecimal](3.1, 3.11, 3.21, 33.1)))

  val parser = new TabDelimitedPlateReaderRawDataParser(PlateDim(4, 3))

  @Test
  def parseSingleSimpleMatrix() = {
    val result = parser(new CharSequenceReader(plate1MatrixInput))
    assert(result successful)
    expect(1) { result.get.size }
    expect(plate1DataParsed) { result.get.first }
  }

  @Test
  def parseMultipleSimpleMatrices() = {
    val n = 3
    val multipleRecords = Seq.fill(n) { plate1MatrixInput }.mkString("\r\n\r\n")
    val result = parser(new CharSequenceReader(multipleRecords))
    assert(result successful)
    expect(3) { result.get.size }
    for (i <- 0 until n) {
      expect(plate1DataParsed) { result.get(i) }
    }
  }

  @Test
  def parseSinglePlateWithMetadata() {
    val result = parser(new CharSequenceReader(plate1RecordInput))
    assert(result successful)
    expect(1) { result.get.size }
    expect(plate1DataParsed) { result.get.first }
  }

  @Test
  def parseMultiplePlatesWithMetadata() {
    val n = 3
    val multipleRecords = Seq.fill(n) { plate1RecordInput }.mkString("\r\n\r\n")
    val result = parser(new CharSequenceReader(multipleRecords))
    assert(result successful)
    expect(3) { result.get.size }
    for (i <- 0 until n) {
      expect(plate1DataParsed) { result.get(i) }
    }
  }

  private def parseMultiplePlatesWithMetadataAndPostscript(postScript: String) {
    val n = 3
    val multipleRecords = Seq.fill(n) { plate1RecordInput }.mkString("\r\n\r\n")
    val result = parser(new CharSequenceReader(multipleRecords + "\r\n" + postScript))
    assert(result successful)
    expect(3) { result.get.size }
    for (i <- 0 until n) {
      expect(plate1DataParsed) { result.get(i) }
    }
  }

  private def parseMultipleFilesMultiplePlatesWithMetadataAndPostscript(postScript: String) {
    val n = 3
    val multipleRecords = Seq.fill(n) { plate1RecordInput }.mkString("\r\n\r\n") + "\r\n" + postScript +
      Seq.fill(n) { plate2RecordInput }.mkString("\r\n\r\n") + "\r\n" + postScript
    val result = parser(new CharSequenceReader(multipleRecords))
    assert(result successful)
    expect(2 * n) { result.get.size }
    for (i <- 0 until n) {
      expect(plate1DataParsed) { result.get(i) }
    }
    for (i <- n until 2 * n) {
      expect(plate2DataParsed) { result.get(i) }
    }
  }

  @Test def parseMultiplePlatesWithMetadataAndPostscript { parseMultiplePlatesWithMetadataAndPostscript(postscriptInput) }
  @Test def parseMultiplePlatesWithMetadataAndEnvisionPostscript { parseMultiplePlatesWithMetadataAndPostscript(envisionPostscriptInput) }
  @Test def parseMultipleFilesMultiplePlatesWithMetadataAndEnvisionPostscript { parseMultipleFilesMultiplePlatesWithMetadataAndPostscript(envisionPostscriptInput) }

  import collection.JavaConversions._

  @Test
  def parseFile() {
    val in = getClass().getResourceAsStream("/sample-plate-data.tab")
    assert(in != null, "test data file not found")
    val plateMatrices: Seq[PlateMatrix] = new TabDelimitedPlateReaderRawDataParser(PlateDim(24, 16)).parse(new java.io.InputStreamReader(in));
    expect(650) { plateMatrices.size }
    expect(314936) { plateMatrices.first.well(0, 0) }
    expect(184323) { plateMatrices.last.well(15, 23) }
    for (p <- plateMatrices) {
      expect(16) { p.height }
      expect(24) { p.width }
    }
  }
}