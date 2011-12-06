package edu.harvard.med.screensaver.io.libraries
import scala.annotation.migration
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator.RegexParsers

object PlateNumbersParser extends RegexParsers with JavaTokenParsers {

  def plateNumbers: Parser[Seq[Int]] = rep(token) ^^ { _.flatten } | failure("the input is interesting, but alas, it is unusable")
  def token: Parser[Seq[Int]] = (plateRange | singlePlate) <~ opt(delim) ^^ { /*TODO: use pattern matching instead!*/ x => if (x.isInstanceOf[Seq[Int]]) x.asInstanceOf[Seq[Int]] else Seq[Int](x.asInstanceOf[Int]) } | failure("this is not going to work")
  def delim: Parser[Any] = ","
  def singlePlate: Parser[Int] = """\d+""".r ^^ { _.toInt } | failure("does this look like a plate number to you?")
  def plateRange: Parser[Seq[Int]] = (singlePlate <~ rangeSeparator) ~ singlePlate ^^ { r => r._1.to(r._2, if (r._1 < r._2) 1 else -1) } | failure("a plate range, this is not")
  def rangeSeparator: Parser[String] = "-" | ".." | ":" | failure("a range separator would be better here")

  def parse(input: String) = parseAll(plateNumbers, input)

  import scala.collection.JavaConversions._
  def parse_Java(input: String): java.util.List[Int] = parse(input).get
  def validate_Java(input: String) = parse(input);
}