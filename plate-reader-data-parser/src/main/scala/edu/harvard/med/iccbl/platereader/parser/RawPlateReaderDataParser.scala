package edu.harvard.med.iccbl.platereader.parser

import edu.harvard.med.iccbl.platereader.PlateMatrix
import scala.util.parsing.combinator.Parsers

trait PlateReaderRawDataParser {
  def parse(in: java.io.Reader): java.util.List[PlateMatrix]
}