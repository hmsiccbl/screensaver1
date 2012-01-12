package edu.harvard.med.iccbl.platereader;
import java.util.ArrayList
import scala.collection.mutable.ArraySeq
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.libraries.PlateSize

case class PlateDim(w: Int, h: Int) {
  val size = w * h
}

object PlateDim {
  implicit def plateSizeToPlateDim(ps: PlateSize): PlateDim = PlateDim(ps.getColumns, ps.getRows)
}

case class PlateMatrix(data: List[List[BigDecimal]]) {

  /** 0-valued matrix */
  def this(dim: PlateDim) = this(List.fill[BigDecimal](dim.h, dim.w) { BigDecimal(0) })

  if (data.size > 1 && data.tail.exists(_.size != data(0).size)) {
    throw new IllegalArgumentException("all rows must have same width")
  }

  val dim = PlateDim(data.headOption.getOrElse(Nil).size, data.size)
  def height = dim.h
  def width = dim.w
  def well(row: Int, col: Int) = data(row)(col)
  def row(row: Int) = data(row)
  /**
   * @return row-major iteration of wells
   */
  def dataIterator = data.flatten.iterator

  def wellNames =
    for {
      r <- 0 until height
      c <- 0 until width
    } yield new WellName(r, c).toString

}