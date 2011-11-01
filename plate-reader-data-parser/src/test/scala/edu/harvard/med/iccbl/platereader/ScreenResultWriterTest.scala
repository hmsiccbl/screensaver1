package edu.harvard.med.iccbl.platereader;

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.FeatureSpec
import org.junit.Test
import org.junit.Ignore
import edu.harvard.med.iccbl.platereader.PlateMatrixTest.makePlateMatrix
import edu.harvard.med.iccbl.platereader.parser.PlateOrdering
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.iccbl.platereader.parser.PlateMetaData
import edu.harvard.med.screensaver.db.LibrariesDAO
import edu.harvard.med.screensaver.model.libraries.LibraryWellType
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType

class ScreenResultWriterTest extends AssertionsForJUnit {
  val ordering = new PlateOrdering().addPlates(1 to 3).
    addReplicates(3).
    addConditions(List("c1", "c2", "c3")).
    addReadoutTypes(List(AssayReadoutType.LUMINESCENCE));
  val controls = Map(new WellName("A01") -> AssayWellControlType.ASSAY_CONTROL,
                     new WellName("B02") -> AssayWellControlType.ASSAY_POSITIVE_CONTROL)
  val wellFinder = new FindWellType { 
                     def apply(wk: WellKey) = wk.getWellName match { 
                       case "A02" => LibraryWellType.EXPERIMENTAL
                       case _ => LibraryWellType.EMPTY
                     }
                   }
  val plateData = for (i <- 1 to ordering.iterator.length) yield makePlateMatrix(i * 100, 2, 2)
  val srw = new ScreenResultWriter(plateData, ordering, controls, wellFinder)

  @Test
  def init {
    expect(9) { srw.colKeys.size }
    expect(3 * 4) { srw.rowKeys.size }
  }

  @Test
  def lookup {
    expect(BigDecimal(100.1)) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(BigDecimal(101.2)) { srw.lookup(new WellKey(1, "B02"), PlateMetaData(replicate = 1, condition = Some("c1"), readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(BigDecimal(200.1)) { srw.lookup(new WellKey(1, "A01"), PlateMetaData(replicate = 1, condition = Some("c2"), readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(BigDecimal(1400.1)) { srw.lookup(new WellKey(2, "A01"), PlateMetaData(replicate = 2, condition = Some("c2"), readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
    expect(BigDecimal(2701.2)) { srw.lookup(new WellKey(3, "B02"), PlateMetaData(replicate = 3, condition = Some("c3"), readoutType = Some(AssayReadoutType.LUMINESCENCE))) }
  }

  @Test
  def write {
    val f = java.io.File.createTempFile("screenResultWriterTest", ".xls");
    //f.deleteOnExit() // comment this out if you need to manually verify the written file
    srw.write(f);
    val expected = List(
      """Plate	Well	Type	Exclude	Luminescence_c1_A	Luminescence_c1_B	Luminescence_c1_C	Luminescence_c2_A	Luminescence_c2_B	Luminescence_c2_C	Luminescence_c3_A	Luminescence_c3_B	Luminescence_c3_C
1	A01	N		100.1	400.1	700.1	200.1	500.1	800.1	300.1	600.1	900.1
1	A02	X		100.2	400.2	700.2	200.2	500.2	800.2	300.2	600.2	900.2
1	B01	E		101.1	401.1	701.1	201.1	501.1	801.1	301.1	601.1	901.1
1	B02	P		101.2	401.2	701.2	201.2	501.2	801.2	301.2	601.2	901.2""",
      """Plate	Well	Type	Exclude	Luminescence_c1_A	Luminescence_c1_B	Luminescence_c1_C	Luminescence_c2_A	Luminescence_c2_B	Luminescence_c2_C	Luminescence_c3_A	Luminescence_c3_B	Luminescence_c3_C
2	A01	N		1000.1	1300.1	1600.1	1100.1	1400.1	1700.1	1200.1	1500.1	1800.1
2	A02	X		1000.2	1300.2	1600.2	1100.2	1400.2	1700.2	1200.2	1500.2	1800.2
2	B01	E		1001.1	1301.1	1601.1	1101.1	1401.1	1701.1	1201.1	1501.1	1801.1
2	B02	P		1001.2	1301.2	1601.2	1101.2	1401.2	1701.2	1201.2	1501.2	1801.2""",
      """Plate	Well	Type	Exclude	Luminescence_c1_A	Luminescence_c1_B	Luminescence_c1_C	Luminescence_c2_A	Luminescence_c2_B	Luminescence_c2_C	Luminescence_c3_A	Luminescence_c3_B	Luminescence_c3_C
3	A01	N		1900.1	2200.1	2500.1	2000.1	2300.1	2600.1	2100.1	2400.1	2700.1
3	A02	X		1900.2	2200.2	2500.2	2000.2	2300.2	2600.2	2100.2	2400.2	2700.2
3	B01	E		1901.1	2201.1	2501.1	2001.1	2301.1	2601.1	2101.1	2401.1	2701.1
3	B02	P		1901.2	2201.2	2501.2	2001.2	2301.2	2601.2	2101.2	2401.2	2701.2""") map { _.split("\r\n").toSeq map { _.split("\t").toSeq } }
    val sheets = for (i <- 0 to 2) yield jxl.Workbook.getWorkbook(f).getSheet(i)
    val actual = for (s <- sheets) yield for (r <- 0 until s.getRows) yield for (c <- 0 until s.getColumns) yield s.getCell(c, r).getContents

    for (platePair <- expected zip actual) 
      println("mismatches=\n" + ((platePair._1.flatten zip platePair._2.flatten) filter ((cellPair) => { cellPair._1 != cellPair._2 })))

    expect(expected)(actual)
  }
}