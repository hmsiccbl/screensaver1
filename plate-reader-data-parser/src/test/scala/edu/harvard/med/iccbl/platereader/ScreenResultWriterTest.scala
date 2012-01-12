package edu.harvard.med.iccbl.platereader;

import java.util.Scanner

import scala.collection.JavaConversions._

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.FeatureSpec

import edu.harvard.med.iccbl.platereader.PlateMatrixTest.makePlateMatrix
import edu.harvard.med.iccbl.platereader.parser.CompositePlateOrdering
import edu.harvard.med.iccbl.platereader.parser.PlateMetaData
import edu.harvard.med.iccbl.platereader.parser.SimplePlateOrdering
import edu.harvard.med.screensaver.io.libraries.ControlWell
import edu.harvard.med.screensaver.io.libraries.WellMetaData
import edu.harvard.med.screensaver.model.libraries.LibraryWellType
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import jxl.write.WritableSheet

// TODO: currently only tests the (simpler) small molecule case; also need tests for RNAi case, which has extended sets of columns and can include the 96-to-384 well plate mapping (quadrant mapping) */ 
class ScreenResultWriterTest extends AssertionsForJUnit {

  val ordering1 = new SimplePlateOrdering().
    addPlates(1 to 3).
    addReplicates(3).
    addConditions(List("c1", "c2", "c3")).
    addReadoutTypes(List(AssayReadoutType.LUMINESCENCE)).
    addReadouts(List("ro1", "ro2"))
  val ordering2 = new SimplePlateOrdering().
    addPlates(List(3, 1)). // note: testing descending plate order and non-contiguous plates
    addReplicates(2).
    addConditions(List("c1")).
    addReadoutTypes(List(AssayReadoutType.FLUORESCENCE_INTENSITY)).
    addReadouts(List("ro1", "ro2", "ro3"))
  val ordering3 = new SimplePlateOrdering().
    addPlates(List(1)).
    addReadoutTypes(List(AssayReadoutType.FP))
  val ordering = new CompositePlateOrdering().add(ordering1).add(ordering2).add(ordering3);
  val controls: Set[WellMetaData] = Set(ControlWell(new WellName("A01"), None, AssayWellControlType.ASSAY_CONTROL),
    ControlWell(new WellName("B02"), None, AssayWellControlType.ASSAY_POSITIVE_CONTROL))
  val wellFinder = new FindWellType {
    def apply(wk: WellKey) = wk.getWellName match {
      case "A02" => LibraryWellType.EXPERIMENTAL
      case _ => LibraryWellType.EMPTY
    }
  }
  val plateData = for (i <- 1 to (54 + 12 + 1)) yield makePlateMatrix(i * 100, 2, 2)
  val quadMapping = new IccblQuadrantMapping(PlateDim(2, 2), PlateDim(2, 2))
  val srw = new ScreenResultWriter(plateData, ordering, controls, wellFinder, PlateDim(2, 2), quadMapping)

  @Test
  def init {
    expect(18 + 6 + 1) { srw.colKeys.size }
    expect(3 * 4) { srw.rowKeys.size }
  }

  @Test
  def lookup {
    expect(None) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 4 /*non-extant*/ , condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(None) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro3") /*non-extant*/ )) }
    expect(None) { srw.lookup(new WellKey(100 /*non-extant*/ , "A01"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }

    expect(Some(BigDecimal(100.1))) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(Some(BigDecimal(101.2))) { srw.lookup(new WellKey(1, "B02"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(Some(BigDecimal(200.1))) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2"))) }
    expect(Some(BigDecimal(201.2))) { srw.lookup(new WellKey(1, "B02"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2"))) }
    expect(Some(BigDecimal(300.1))) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c2"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(Some(BigDecimal(301.2))) { srw.lookup(new WellKey(1, "B02"), PlateMetaData(replicate = 1, condition = Some("c2"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(Some(BigDecimal(2700.1))) { srw.lookup(new WellKey(2, "A01"), PlateMetaData(replicate = 2, condition = Some("c2"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1"))) }
    expect(Some(BigDecimal(5401.2))) { srw.lookup(new WellKey(3, "B02"), PlateMetaData(replicate = 3, condition = Some("c3"), readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2"))) }
  }

  @Test
  def lookupQuadrants {
    val plateData = for (i <- 1 to 8) yield makePlateMatrix(i * 100, 2, 2)
    val ordering = new SimplePlateOrdering().
      addPlates(1 to 2).
      addQuadrants(4).
      addReadoutTypes(List(AssayReadoutType.LUMINESCENCE))
    val srw = new ScreenResultWriter(plateData, ordering, controls, wellFinder, PlateDim(4, 4), new IccblQuadrantMapping(PlateDim(2, 2), PlateDim(4, 4)))
    expect(Some(BigDecimal(100.1))) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(quadrant = 0, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(200.1))) { srw.lookup(new WellKey(1, "A02"), PlateMetaData(quadrant = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(300.1))) { srw.lookup(new WellKey(1, "B01"), PlateMetaData(quadrant = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(400.1))) { srw.lookup(new WellKey(1, "B02"), PlateMetaData(quadrant = 3, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }

    expect(Some(BigDecimal(101.2))) { srw.lookup(new WellKey(1, "C03"), PlateMetaData(quadrant = 0, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(201.2))) { srw.lookup(new WellKey(1, "C04"), PlateMetaData(quadrant = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(301.2))) { srw.lookup(new WellKey(1, "D03"), PlateMetaData(quadrant = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(401.2))) { srw.lookup(new WellKey(1, "D04"), PlateMetaData(quadrant = 3, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }

    expect(Some(BigDecimal(500.1))) { srw.lookup(new WellKey(2, "A01"), PlateMetaData(quadrant = 0, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(600.1))) { srw.lookup(new WellKey(2, "A02"), PlateMetaData(quadrant = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(700.1))) { srw.lookup(new WellKey(2, "B01"), PlateMetaData(quadrant = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(800.1))) { srw.lookup(new WellKey(2, "B02"), PlateMetaData(quadrant = 3, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }

    expect(Some(BigDecimal(501.2))) { srw.lookup(new WellKey(2, "C03"), PlateMetaData(quadrant = 0, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(601.2))) { srw.lookup(new WellKey(2, "C04"), PlateMetaData(quadrant = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(701.2))) { srw.lookup(new WellKey(2, "D03"), PlateMetaData(quadrant = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(Some(BigDecimal(801.2))) { srw.lookup(new WellKey(2, "D04"), PlateMetaData(quadrant = 3, readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
  }

  def tokenizer(in: String, delim: String) = {
    // it's s**t like this that makes me want a different career...
    def trailingEmptyTokenHack(in: String) = if (in.endsWith(delim)) in + delim else in
    for (t <- Seq() ++ new Scanner(trailingEmptyTokenHack(in)).useDelimiter(delim)) yield t
  }

  @Test
  def tokenizerTrailingEmptyTokenHack {
    expect(Seq("1", "2", "3", "")) { tokenizer("1\t2\t3\t", "\t") }
  }

  @Test
  def write {
    val f = java.io.File.createTempFile("screenResultWriterTest", ".xls");
    //f.deleteOnExit() // comment this out if you need to manually verify the written file
    srw.write(f)
    val inputData = List(
      """Plate	Well	Type	Exclude	ro1_Luminescence_c1_A	ro2_Luminescence_c1_A	ro1_Luminescence_c2_A	ro2_Luminescence_c2_A	ro1_Luminescence_c3_A	ro2_Luminescence_c3_A	ro1_Luminescence_c1_B	ro2_Luminescence_c1_B	ro1_Luminescence_c2_B	ro2_Luminescence_c2_B	ro1_Luminescence_c3_B	ro2_Luminescence_c3_B	ro1_Luminescence_c1_C	ro2_Luminescence_c1_C	ro1_Luminescence_c2_C	ro2_Luminescence_c2_C	ro1_Luminescence_c3_C	ro2_Luminescence_c3_C	ro1_Fluorescence Intensity_c1_A	ro2_Fluorescence Intensity_c1_A	ro3_Fluorescence Intensity_c1_A	ro1_Fluorescence Intensity_c1_B	ro2_Fluorescence Intensity_c1_B	ro3_Fluorescence Intensity_c1_B	FP
1	A01	N		100.1	200.1	300.1	400.1	500.1	600.1	700.1	800.1	900.1	1000.1	1100.1	1200.1	1300.1	1400.1	1500.1	1600.1	1700.1	1800.1	6100.1	6200.1	6300.1	6400.1	6500.1	6600.1	6700.1
1	A02	X		100.2	200.2	300.2	400.2	500.2	600.2	700.2	800.2	900.2	1000.2	1100.2	1200.2	1300.2	1400.2	1500.2	1600.2	1700.2	1800.2	6100.2	6200.2	6300.2	6400.2	6500.2	6600.2	6700.2
1	B01	E		101.1	201.1	301.1	401.1	501.1	601.1	701.1	801.1	901.1	1001.1	1101.1	1201.1	1301.1	1401.1	1501.1	1601.1	1701.1	1801.1	6101.1	6201.1	6301.1	6401.1	6501.1	6601.1	6701.1
1	B02	P		101.2	201.2	301.2	401.2	501.2	601.2	701.2	801.2	901.2	1001.2	1101.2	1201.2	1301.2	1401.2	1501.2	1601.2	1701.2	1801.2	6101.2	6201.2	6301.2	6401.2	6501.2	6601.2	6701.2""",
      """Plate	Well	Type	Exclude	ro1_Luminescence_c1_A	ro2_Luminescence_c1_A	ro1_Luminescence_c2_A	ro2_Luminescence_c2_A	ro1_Luminescence_c3_A	ro2_Luminescence_c3_A	ro1_Luminescence_c1_B	ro2_Luminescence_c1_B	ro1_Luminescence_c2_B	ro2_Luminescence_c2_B	ro1_Luminescence_c3_B	ro2_Luminescence_c3_B	ro1_Luminescence_c1_C	ro2_Luminescence_c1_C	ro1_Luminescence_c2_C	ro2_Luminescence_c2_C	ro1_Luminescence_c3_C	ro2_Luminescence_c3_C	ro1_Fluorescence Intensity_c1_A	ro2_Fluorescence Intensity_c1_A	ro3_Fluorescence Intensity_c1_A	ro1_Fluorescence Intensity_c1_B	ro2_Fluorescence Intensity_c1_B	ro3_Fluorescence Intensity_c1_B	FP
2	A01	N		1900.1	2000.1	2100.1	2200.1	2300.1	2400.1	2500.1	2600.1	2700.1	2800.1	2900.1	3000.1	3100.1	3200.1	3300.1	3400.1	3500.1	3600.1							
2	A02	X		1900.2	2000.2	2100.2	2200.2	2300.2	2400.2	2500.2	2600.2	2700.2	2800.2	2900.2	3000.2	3100.2	3200.2	3300.2	3400.2	3500.2	3600.2							
2	B01	E		1901.1	2001.1	2101.1	2201.1	2301.1	2401.1	2501.1	2601.1	2701.1	2801.1	2901.1	3001.1	3101.1	3201.1	3301.1	3401.1	3501.1	3601.1							
2	B02	P		1901.2	2001.2	2101.2	2201.2	2301.2	2401.2	2501.2	2601.2	2701.2	2801.2	2901.2	3001.2	3101.2	3201.2	3301.2	3401.2	3501.2	3601.2							""",
      """Plate	Well	Type	Exclude	ro1_Luminescence_c1_A	ro2_Luminescence_c1_A	ro1_Luminescence_c2_A	ro2_Luminescence_c2_A	ro1_Luminescence_c3_A	ro2_Luminescence_c3_A	ro1_Luminescence_c1_B	ro2_Luminescence_c1_B	ro1_Luminescence_c2_B	ro2_Luminescence_c2_B	ro1_Luminescence_c3_B	ro2_Luminescence_c3_B	ro1_Luminescence_c1_C	ro2_Luminescence_c1_C	ro1_Luminescence_c2_C	ro2_Luminescence_c2_C	ro1_Luminescence_c3_C	ro2_Luminescence_c3_C	ro1_Fluorescence Intensity_c1_A	ro2_Fluorescence Intensity_c1_A	ro3_Fluorescence Intensity_c1_A	ro1_Fluorescence Intensity_c1_B	ro2_Fluorescence Intensity_c1_B	ro3_Fluorescence Intensity_c1_B	FP
3	A01	N		3700.1	3800.1	3900.1	4000.1	4100.1	4200.1	4300.1	4400.1	4500.1	4600.1	4700.1	4800.1	4900.1	5000.1	5100.1	5200.1	5300.1	5400.1	5500.1	5600.1	5700.1	5800.1	5900.1	6000.1	
3	A02	X		3700.2	3800.2	3900.2	4000.2	4100.2	4200.2	4300.2	4400.2	4500.2	4600.2	4700.2	4800.2	4900.2	5000.2	5100.2	5200.2	5300.2	5400.2	5500.2	5600.2	5700.2	5800.2	5900.2	6000.2	
3	B01	E		3701.1	3801.1	3901.1	4001.1	4101.1	4201.1	4301.1	4401.1	4501.1	4601.1	4701.1	4801.1	4901.1	5001.1	5101.1	5201.1	5301.1	5401.1	5501.1	5601.1	5701.1	5801.1	5901.1	6001.1	
3	B02	P		3701.2	3801.2	3901.2	4001.2	4101.2	4201.2	4301.2	4401.2	4501.2	4601.2	4701.2	4801.2	4901.2	5001.2	5101.2	5201.2	5301.2	5401.2	5501.2	5601.2	5701.2	5801.2	5901.2	6001.2	""")

    val expected = inputData map { tokenizer(_, """\r\n""") map { tokenizer(_, "\t") } }
    expect(5) { expected(1).size }
    expect(29) { expected(1)(1).size }
    val sheets = for (i <- 0 to 2) yield jxl.Workbook.getWorkbook(f).getSheet(i)
    val actual = for (s <- sheets) yield for (r <- 0 until s.getRows) yield for (c <- 0 until s.getColumns) yield s.getCell(c, r).getContents

    // generate output for debugging this test when it fails
    for (platePair <- expected zip actual)
      println("mismatches=\n" + ((platePair._1.flatten zip platePair._2.flatten) filter ((cellPair) => { cellPair._1 != cellPair._2 })))

    expect(expected)(actual)
  }

  @Test
  def auxiliaryColumns {
    val f = java.io.File.createTempFile("screenResultWriterTest", ".xls");
    f.deleteOnExit() // comment this out if you need to manually verify the written file

    srw.colWriters += new ColumnsWriter() {
      val headers = Seq("Aux")
      def writeValues(s: WritableSheet, wk: WellKey, r: Int, c: Int) = { s.addCell(new jxl.write.Label(c, r, "aux" + r)); 1 }
    }
    srw.write(f)
    val sheet = jxl.Workbook.getWorkbook(f).getSheet(0)
    val auxColData = for (r <- 1 until sheet.getRows) yield sheet.getCell(29, r).getContents
    assert(auxColData.forall(_.matches("""aux\d+""")))
  }
}