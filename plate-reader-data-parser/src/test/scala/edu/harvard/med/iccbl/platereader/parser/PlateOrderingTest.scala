package edu.harvard.med.iccbl.platereader.parser

import org.scalatest.junit.AssertionsForJUnit
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import org.junit.Test
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.iccbl.platereader.PlateMatrixTest.makePlateMatrix

class PlateOrderingTest extends AssertionsForJUnit {
  @Test
  def fullOrdering() {
    val ordering = new PlateOrdering().addPlates(List(1, 2, 4)).addConditions(List("c1", "c2")).addReplicates(2).addReadoutTypes(List(AssayReadoutType.LUMINESCENCE))
    expect(List(
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(1), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(1), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(2), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(2), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(4), condition = Some("c1"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 1, readoutType = Some(AssayReadoutType.LUMINESCENCE)),
      PlateMetaData(plate = Some(4), condition = Some("c2"), replicate = 2, readoutType = Some(AssayReadoutType.LUMINESCENCE)))) { ordering.iterator.toList }
  }

  @Test
  def partialOrdering() {
    val ordering = new PlateOrdering().addReplicates(2).addPlates(List(1, 2, 4))
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
    val plates = for (i <- 1 to 8) yield makePlateMatrix(100 * i)
    val ordering = new PlateOrdering().addPlates(1 to 2).addReplicates(2).addConditions(List("1hr", "4hr"))
    expect(Map(
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("1hr")) -> makePlateMatrix(100),
      PlateMetaData(plate = Some(1), replicate = 1, condition = Some("4hr")) -> makePlateMatrix(200),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("1hr")) -> makePlateMatrix(300),
      PlateMetaData(plate = Some(1), replicate = 2, condition = Some("4hr")) -> makePlateMatrix(400),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("1hr")) -> makePlateMatrix(500),
      PlateMetaData(plate = Some(2), replicate = 1, condition = Some("4hr")) -> makePlateMatrix(600),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("1hr")) -> makePlateMatrix(700),
      PlateMetaData(plate = Some(2), replicate = 2, condition = Some("4hr")) -> makePlateMatrix(800))) { ordering.collate(plates) }
  }
}
