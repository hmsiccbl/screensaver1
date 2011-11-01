package edu.harvard.med.iccbl.platereader;
import java.util.ArrayList
import scala.collection.mutable.ArraySeq
import edu.harvard.med.screensaver.model.libraries.WellName

case class PlateMatrix(data: List[List[BigDecimal]]) {

  if (data.size > 1 && data.tail.exists(_.size != data(0).size)) {
    throw new IllegalArgumentException("all rows must have same width")
  }

  /** 0-valued matrix */
  def this(height: Int, width: Int) = this(List.fill[BigDecimal](height, width) { BigDecimal(0) })

  val height = data.size
  val width = data.headOption.getOrElse(Nil).size

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