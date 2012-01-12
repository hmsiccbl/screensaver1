package edu.harvard.med.screensaver.io.libraries
import scala.annotation.migration
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator.RegexParsers
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.libraries.PlateSize
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType

abstract case class WellMetaData(wellName: WellName, label: Option[String] = None)
case class ControlWell(wellName2: WellName, label2: Option[String] = None, controlType: AssayWellControlType) extends WellMetaData(wellName2, label2)
case class WellNameLabel(wellName2: WellName, label2: Option[String] = None) extends WellMetaData(wellName2, label2)

class ControlWellsParser(plateSize: PlateSize, controlType: AssayWellControlType) extends WellNamesParser[ControlWell](plateSize) {
  def makeItem(wellName: WellName, label: Option[String]) = ControlWell(wellName, label, controlType)
}

class LibraryControlWellsParser(plateSize: PlateSize) extends WellNamesParser[WellNameLabel](plateSize) {
  def makeItem(wellName: WellName, label: Option[String]) = WellNameLabel(wellName, label)
}

abstract class WellNamesParser[T <: WellMetaData](plateSize: PlateSize) extends RegexParsers with JavaTokenParsers {
  val wellColumnRegex = """[0-9]+""".r
  val wellRowRegex = """[A-Za-z]+""".r

  def makeItem(wellName: WellName, label: Option[String]): T

  def wells: Parser[Seq[T]] = rep(token) ^^ { _.flatten }
  def token: Parser[Seq[T]] = ((wellRectangle | singleWellName | wellColumn | wellRow) ~ opt(controlLabel)) <~ delim ^^ {
    e =>
      e._1 match {
        case s: Seq[WellName] => s.map { w => makeItem(w, e._2) }
        case w: WellName => Seq(makeItem(w, e._2))
      }
  }
  def wellRectangle: Parser[Seq[WellName]] = ((singleWellName <~ rangeDelim) ~ singleWellName) ^^ { x => makeRectangle(x._1, x._2) }
  def singleWellName: Parser[WellName] = wellRowRegex ~ wellColumnRegex ^^ { x => new WellName(x._1 + x._2) }
  def wellColumn: Parser[Seq[WellName]] = opt("""col.*""".r) ~> wellColumnRegex ^^ { makeColumn(_) }
  def wellRow: Parser[Seq[WellName]] = opt("""row.*""".r) ~> wellRowRegex ^^ { makeRow(_) }
  def rangeDelim: Parser[String] = ".." | "-" | ":"
  def delim: Parser[Any] = opt(",")
  def controlLabel: Parser[String] = "=" ~> ("""[\w|]+""".r | ("\"" ~> """[^"]+""".r <~ "\""))

  def parse(input: String) = parseAll(wells, input)

  import scala.collection.JavaConversions._
  def parse_Java(input: String): java.util.List[T] = parse(input).get
  def validate_Java(input: String) = parse(input);

  private def makeRow(row: String) = for (col <- plateSize.getColumnsLabels) yield new WellName(row + col)
  private def makeColumn(col: String) = for (row <- plateSize.getRowsLabels) yield new WellName(row + col)
  private def makeRectangle(ul: WellName, lr: WellName) =
    for {
      iRow <- ul.getRowIndex to lr.getRowIndex
      iCol <- ul.getColumnIndex to lr.getColumnIndex
    } yield new WellName(iRow, iCol)

}