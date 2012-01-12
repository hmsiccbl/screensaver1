package edu.harvard.med.screensaver.service.screenresult

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.screensaver.model.libraries.PlateSize
import edu.harvard.med.screensaver.io.libraries.ControlWell
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType

class PlateReaderRawDataTransformerTest extends AssertionsForJUnit {

  val plateReaderRawDataTransformer = new PlateReaderRawDataTransformer(null)

  @Test
  def testHeterogenousMapControlsToLibraryPlateSize() {
    val mappedControls = plateReaderRawDataTransformer.mapControlsToLibraryPlateSize(Set(ControlWell(new WellName("A01"), Some("c1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B02"), Some("c2"), AssayWellControlType.ASSAY_POSITIVE_CONTROL),
      ControlWell(new WellName("H12"), Some("c3"), AssayWellControlType.OTHER_CONTROL)),
      PlateSize.WELLS_96,
      PlateSize.WELLS_384)
    val expected = Set(
      ControlWell(new WellName("A01"), Some("c1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("A02"), Some("c1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B01"), Some("c1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B02"), Some("c1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("C03"), Some("c2"), AssayWellControlType.ASSAY_POSITIVE_CONTROL),
      ControlWell(new WellName("C04"), Some("c2"), AssayWellControlType.ASSAY_POSITIVE_CONTROL),
      ControlWell(new WellName("D03"), Some("c2"), AssayWellControlType.ASSAY_POSITIVE_CONTROL),
      ControlWell(new WellName("D04"), Some("c2"), AssayWellControlType.ASSAY_POSITIVE_CONTROL),
      ControlWell(new WellName("O23"), Some("c3"), AssayWellControlType.OTHER_CONTROL),
      ControlWell(new WellName("O24"), Some("c3"), AssayWellControlType.OTHER_CONTROL),
      ControlWell(new WellName("P23"), Some("c3"), AssayWellControlType.OTHER_CONTROL),
      ControlWell(new WellName("P24"), Some("c3"), AssayWellControlType.OTHER_CONTROL))
    expect(expected)(mappedControls.filter { _.label.isDefined })
  }

  @Test
  def testMapHomogeneousControlsToLibraryPlateSize() {
    def doMapControlsToLibraryPlateSize(l: String) =
      plateReaderRawDataTransformer.mapControlsToLibraryPlateSize(Set(ControlWell(new WellName("A01"), Some(l), AssayWellControlType.ASSAY_CONTROL)), PlateSize.WELLS_96, PlateSize.WELLS_384).filter { _.label.isDefined }
    expect(Set(
      ControlWell(new WellName("A01"), Some("q1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("A02"), Some("q2"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B01"), Some("q3"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B02"), Some("q4"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("q1|q2|q3|q4") }
    expect(Set(
      ControlWell(new WellName("A01"), Some("q1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B01"), Some("q3"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B02"), Some("q4"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("q1||q3|q4") }
    expect(Set(
      ControlWell(new WellName("A01"), Some("q1"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("A02"), Some("q2"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B01"), Some("q3"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("q1|q2|q3|") }
    expect(Set(
      ControlWell(new WellName("A02"), Some("q2"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B01"), Some("q3"), AssayWellControlType.ASSAY_CONTROL),
      ControlWell(new WellName("B02"), Some("q4"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("|q2|q3|q4") }
    //    expect(Set(ControlWell(new WellName("A02"), Some("q2"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("|q2") } // not currently handled by the parser!
    expect(Set(ControlWell(new WellName("A02"), Some("q2"), AssayWellControlType.ASSAY_CONTROL))) { doMapControlsToLibraryPlateSize("|q2||") }
    expect(Set()) { doMapControlsToLibraryPlateSize("|||") }
  }

}