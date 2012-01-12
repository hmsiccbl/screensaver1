package edu.harvard.med.iccbl.platereader;

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.FeatureSpec
import org.junit.Test
import org.junit.Ignore

object PlateMatrixTest {
  def makePlateMatrix(baseValue: BigDecimal, rows: Int = 3, cols: Int = 3) = {
    val data = for (r <- List.range(0, rows)) yield for (c <- List.range(1, cols + 1)) yield baseValue + r + BigDecimal(c) / 10
    new PlateMatrix(data)
  }
}

class PlateMatrixTest extends AssertionsForJUnit {

  @Test
  def badMatrix {
    intercept[IllegalArgumentException] { PlateMatrix(List(List(1, 2, 3), List(3, 4), List(7, 8, 9))) }
  }

  @Test
  def smallestMatrix {
    val plate = PlateMatrix(List(List(1)));
    expect(1) { plate.well(0, 0) }
  }

  @Test
  def fullMatrix {
    val plate = PlateMatrix(List(List(1, 2, 3), List(3, 4, 5)))
    expect(2) { plate.height }
    expect(3) { plate.width }
    expect(BigDecimal(1)) { plate.well(0, 0) }
    expect(BigDecimal(5)) { plate.well(1, 2) }
  }

  @Test
  def emptyMatrix {
    val plate = new PlateMatrix(PlateDim(24, 16))
    expect(16) { plate.height }
    expect(24) { plate.width }
    expect(BigDecimal(0)) { plate.well(0, 0) }
    expect(BigDecimal(0)) { plate.well(15, 23) }
    for (r <- 1 to 16)
      for (c <- 1 to 24)
        expect(BigDecimal(0)) { plate.well(r - 1, c - 1) }
    intercept[java.lang.IndexOutOfBoundsException] { plate.well(0, 24) }
    intercept[java.lang.IndexOutOfBoundsException] { plate.well(16, 0) }
  }

  @Test
  def wellIteratorIsRowMajor {
    val plate = PlateMatrix(List(List(1, 2, 3), List(4, 5, 6)))
    expect(List(1, 2, 3, 4, 5, 6)) { plate.dataIterator.toList }
  }

  @Test
  def wellNames {
    val plate = new PlateMatrix(PlateDim(3, 2))
    expect(List("A01", "A02", "A03", "B01", "B02", "B03")) { plate.wellNames.toList }
  }
}

