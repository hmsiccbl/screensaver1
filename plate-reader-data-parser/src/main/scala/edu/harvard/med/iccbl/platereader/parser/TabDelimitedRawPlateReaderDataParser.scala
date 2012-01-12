package edu.harvard.med.iccbl.platereader.parser;

import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.CharSequenceReader
import org.apache.commons.io.IOUtils
import edu.harvard.med.iccbl.platereader.PlateMatrix
import java.io.StringReader
import scala.util.parsing.input.Reader
import edu.harvard.med.iccbl.platereader.PlateDim

class TabDelimitedPlateReaderRawDataParser(dim: PlateDim) extends RegexParsers with JavaTokenParsers with PlateReaderRawDataParser {

  class MetaDataLineParser extends Parser[String] {
    type Elem = Char

    // note: '(?m)' sets MULTILINE regex option, allowing the '^' and '$' to match after/before any type of newline ("\n", "\r\n", etc.)
    def metaDataLine: Parser[String] = """(?m)^.*$""".r

    def apply(in: Input) =
      if (parse(plateColumnHeaders, in).successful) {
        Failure("at plate column headers line, so not a meta data line", in)
      } else {
        parse(metaDataLine, in)
      }
  }

  def plateMatrices: Parser[List[PlateMatrix]] = rep(plateRecord) <~ opt(postscript)
  def postscript: Parser[Any] = metadata
  def plateRecord: Parser[PlateMatrix] = metadata ~> plateMatrix
  def metadata: Parser[Any] = rep(new MetaDataLineParser())
  def plateMatrix: Parser[PlateMatrix] = plateColumnHeaders ~> plateDataRows ^^ { new PlateMatrix(_) }
  def plateColumnHeaders: Parser[Any] = repN(dim.w, columnHeaderLabel)
  def columnHeaderLabel: Parser[Any] = wholeNumber
  def plateDataRows: Parser[List[List[BigDecimal]]] = repN(dim.h, plateDataRow)
  def plateDataRow: Parser[List[BigDecimal]] = rowHeaderLabel ~> repN(dim.w, datum)
  def rowHeaderLabel: Parser[Any] = "[A-Z]".r
  def datum: Parser[BigDecimal] = floatingPointNumber ^^ { BigDecimal(_) }

  def apply(in: Input) = parseAll(plateMatrices, EnvisionRawDataCleaner.clean(in))

  class ParseException(result: ParseResult[Any]) extends RuntimeException

  import collection.JavaConversions._

  def parse(in: java.io.Reader): java.util.List[PlateMatrix] = {
    // TODO: streamed reader
    //    class JavaReaderAdapter extends PagedSeqReader
    val s = IOUtils.toString(in)
    val result = this(new CharSequenceReader(s))
    if (result.successful) result.get
    else throw new RuntimeException(result.toString)
  }
}

object EnvisionRawDataCleaner extends RawDataCleaner {
  val postscriptPrefix = "Basic assay information";
  val postscriptSuffix = "Exported with EnVision Workstation version 1.12";
  def clean(in: Reader[Char]) = {
    // TOOD: this may be all we need for this function 
    // new CharSequenceReader(r.replaceAllIn(in.source.toString, ""))

    val s = in.source.toString
    if (s.contains(postscriptPrefix)) {
      if (s.contains(postscriptSuffix))
        clean(new CharSequenceReader(s.substring(0, s.indexOf(postscriptPrefix)) + s.substring(s.indexOf(postscriptSuffix) + postscriptSuffix.length())))
      else {
        // warn the user, since failure to locate the suffix may mean that the wrong version of Envision input file is being used, and multiple file information is lost as well - sde4
        new CharSequenceReader(s.substring(0, s.indexOf(postscriptPrefix)))
      }
    } else
      in
  }
}