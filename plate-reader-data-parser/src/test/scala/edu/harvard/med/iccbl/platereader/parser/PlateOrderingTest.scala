package edu.harvard.med.iccbl.platereader.parser

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

import edu.harvard.med.iccbl.platereader.PlateMatrixTest.makePlateMatrix
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.screensaver.model.screens.AssayReadoutType

class PlateOrderingTest extends AssertionsForJUnit {
  @Test
  def allPropertiesSimpleOrdering() {
    val ordering = new SimplePlateOrdering().addReadoutTypes(List(AssayReadoutType.LUMINESCENCE)).addPlates(List(1, 2, 4)).addConditions(List("c1", "c2")).addReplicates(2).addReadouts(List("ro1", "ro2"))
    expect(List(
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro1")),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE), readout = Some("ro2")))) { ordering.iterator.toList }
  }

  @Test
  def partialPropertiesSinglePlateOrdering() {
    val ordering = new SimplePlateOrdering().addReplicates(2).addPlates(List(1, 2, 4))
    expect(List(
      PlateMetaData(plate = Some(1), replicate = 1),
      PlateMetaData(plate = Some(2), replicate = 1),
      PlateMetaData(plate = Some(4), replicate = 1),
      PlateMetaData(plate = Some(1), replicate = 2),
      PlateMetaData(plate = Some(2), replicate = 2),
      PlateMetaData(plate = Some(4), replicate = 2))) { ordering.iterator.toList }
  }

  @Test
  def collate() {
    val plates = for (i <- 1 to 16) yield makePlateMatrix(100 * i)
    val ordering = new SimplePlateOrdering().addPlates(1 to 2).addReplicates(2).addConditions(List("1hr", "4hr")).addReadouts(List("ro1", "ro2"))
    expect(Map(
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("1hr"), readout = Some("ro1")) -> makePlateMatrix(100),
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("1hr"), readout = Some("ro2")) -> makePlateMatrix(200),
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("4hr"), readout = Some("ro1")) -> makePlateMatrix(300),
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("4hr"), readout = Some("ro2")) -> makePlateMatrix(400),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("1hr"), readout = Some("ro1")) -> makePlateMatrix(500),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("1hr"), readout = Some("ro2")) -> makePlateMatrix(600),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("4hr"), readout = Some("ro1")) -> makePlateMatrix(700),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("4hr"), readout = Some("ro2")) -> makePlateMatrix(800),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("1hr"), readout = Some("ro1")) -> makePlateMatrix(900),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("1hr"), readout = Some("ro2")) -> makePlateMatrix(1000),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("4hr"), readout = Some("ro1")) -> makePlateMatrix(1100),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("4hr"), readout = Some("ro2")) -> makePlateMatrix(1200),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("1hr"), readout = Some("ro1")) -> makePlateMatrix(1300),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("1hr"), readout = Some("ro2")) -> makePlateMatrix(1400),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("4hr"), readout = Some("ro1")) -> makePlateMatrix(1500),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("4hr"), readout = Some("ro2")) -> makePlateMatrix(1600))) { ordering.collate(plates) }
  }

  @Test
  def multiPlateOrdering() {
    val ordering = new CompositePlateOrdering().
      add(new SimplePlateOrdering().addReadoutTypes(List(AssayReadoutType.FP)).addReplicates(2).addPlates(List(1, 4))).
      add(new SimplePlateOrdering().addReadoutTypes(List(AssayReadoutType.FP)).addReadouts(List("RO1", "RO2")).addPlates(List(1, 2))).
      add(new SimplePlateOrdering().addReadoutTypes(List(AssayReadoutType.FRET)).addConditions(List("C1", "C2", "C3")).addPlates(List(3, 4)));
    expect(List(
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), replicate = 1),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FP), replicate = 1),
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), replicate = 2),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FP), replicate = 2),

      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), readout = Some("RO1")),
      PlateMetaData(plate = Some(2), readoutType = Some(AssayReadoutType.FP), readout = Some("RO1")),
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), readout = Some("RO2")),
      PlateMetaData(plate = Some(2), readoutType = Some(AssayReadoutType.FP), readout = Some("RO2")),

      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C1")),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C1")),
      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C2")),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C2")),
      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C3")),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C3")))) { ordering.iterator.toList }

    val plates = for (i <- 1 to 16) yield makePlateMatrix(100 * i)
    expect(Map(
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), replicate = 1) -> makePlateMatrix(100),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FP), replicate = 1) -> makePlateMatrix(200),
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), replicate = 2) -> makePlateMatrix(300),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FP), replicate = 2) -> makePlateMatrix(400),

      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), readout = Some("RO1")) -> makePlateMatrix(500),
      PlateMetaData(plate = Some(2), readoutType = Some(AssayReadoutType.FP), readout = Some("RO1")) -> makePlateMatrix(600),
      PlateMetaData(plate = Some(1), readoutType = Some(AssayReadoutType.FP), readout = Some("RO2")) -> makePlateMatrix(700),
      PlateMetaData(plate = Some(2), readoutType = Some(AssayReadoutType.FP), readout = Some("RO2")) -> makePlateMatrix(800),

      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C1")) -> makePlateMatrix(900),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C1")) -> makePlateMatrix(1000),
      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C2")) -> makePlateMatrix(1100),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C2")) -> makePlateMatrix(1200),
      PlateMetaData(plate = Some(3), readoutType = Some(AssayReadoutType.FRET), condition = Some("C3")) -> makePlateMatrix(1300),
      PlateMetaData(plate = Some(4), readoutType = Some(AssayReadoutType.FRET), condition = Some("C3")) -> makePlateMatrix(1400))) { ordering.collate(plates) }
  }

  @Test
  def emptyOrderingInputs() {
    val ordering = new SimplePlateOrdering().addPlates(List(1, 4)).addReadoutTypes(List(AssayReadoutType.FP)).addConditions(Nil).addReadouts(Nil)
    expect(2) { ordering.iterator.size }
  }

}
