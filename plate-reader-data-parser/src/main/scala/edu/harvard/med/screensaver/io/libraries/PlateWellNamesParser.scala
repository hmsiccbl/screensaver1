package edu.harvard.med.screensaver.io.libraries
import scala.annotation.migration
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator.RegexParsers
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.libraries.PlateSize

class PlateWellNamesParser(plateSize: PlateSize) extends RegexParsers with JavaTokenParsers {
  val wellColumnRegex = """[0-9]+""".r
  val wellRowRegex = """[A-Za-z]+""".r

  def wells: Parser[Seq[WellName]] = rep(token) ^^ { _.flatten }
  def token: Parser[Seq[WellName]] = (wellRectangle | singleWellName | wellColumn | wellRow) <~ delim ^^ { /*TODO: use pattern matching instead!*/ x => if (x.isInstanceOf[Seq[WellName]]) x.asInstanceOf[Seq[WellName]] else Seq[WellName](x.asInstanceOf[WellName]) }
  def wellRectangle: Parser[Seq[WellName]] = ((singleWellName <~ rangeDelim) ~ singleWellName) ^^ { x => makeRectangle(x._1, x._2) }
  def singleWellName: Parser[WellName] = wellRowRegex ~ wellColumnRegex ^^ { x => new WellName(x._1 + x._2) }
  def wellColumn: Parser[Seq[WellName]] = opt("""col.*""".r) ~> wellColumnRegex ^^ { makeColumn(_) }
  def wellRow: Parser[Seq[WellName]] = opt("""row.*""".r) ~> wellRowRegex ^^ { makeRow(_) }
  def rangeDelim: Parser[String] = ".." | "-" | ":"
  def delim: Parser[Any] = opt(",")

  def parse(input: String) = parseAll(wells, input)

  import scala.collection.JavaConversions._
  def parse_Java(input: String): java.util.List[WellName] = parse(input).get
  def validate_Java(input: String) = parse(input);

  private def makeRow(row: String) = for (col <- plateSize.getColumnsLabels) yield new WellName(row + col)
  private def makeColumn(col: String) = for (row <- plateSize.getRowsLabels) yield new WellName(row + col)
  private def makeRectangle(ul: WellName, lr: WellName) =
    for {
      iRow <- ul.getRowIndex to lr.getRowIndex
      iCol <- ul.getColumnIndex to lr.getColumnIndex
    } yield new WellName(iRow, iCol)

}